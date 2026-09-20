<?php

namespace App\Controllers\Panel;

use App\Controllers\BaseController;
use App\Libraries\FirestoreRepository;
use App\Libraries\PanelPermissions;
use App\Libraries\ResourceManager;
use CodeIgniter\HTTP\ResponseInterface;
use Throwable;

/**
 * Autenticación del panel web.
 *
 * Dos vías, sin crear un sistema paralelo:
 *  1. Administrador de respaldo configurado en .env (Nica.adminUser/Password).
 *  2. Cuentas de usuario de la app: se validan contra Firebase Authentication
 *     (Identity Toolkit) y el rol se lee de Firestore (usuarios/{uid}.rol).
 *
 * Solo ADMIN, EDITOR y AUDITOR pueden entrar. El rol USUARIO no tiene acceso.
 */
class Auth extends BaseController
{
    public function loginForm()
    {
        if (session()->get('nica_admin') === true) {
            return redirect()->to(site_url('panel'));
        }

        return view('panel/login', ['title' => 'Iniciar sesión']);
    }

    public function attempt(): ResponseInterface
    {
        $username = trim((string) $this->request->getPost('username'));
        $password = (string) $this->request->getPost('password');

        // 1) Administrador de respaldo (.env).
        if ($this->isEnvAdmin($username, $password)) {
            return $this->startSession($username, PanelPermissions::ROLE_ADMIN, null, $username);
        }

        // 2) Cuenta Firebase con rol en Firestore.
        $account = $this->firebaseSignIn($username, $password);
        if ($account === null) {
            return redirect()->back()->with('error', 'Usuario o contraseña incorrectos.');
        }

        $rol = $this->roleFromFirestore((string) $account['uid']);

        if (! PanelPermissions::canAccessPanel($rol)) {
            return redirect()->back()->with('error', 'Tu cuenta no tiene acceso al panel administrativo.');
        }

        return $this->startSession(
            (string) ($account['nombre'] !== '' ? $account['nombre'] : $username),
            $rol,
            (string) $account['uid'],
            $username
        );
    }

    public function logout(): ResponseInterface
    {
        session()->destroy();

        return redirect()->to(site_url('panel/login'));
    }

    private function isEnvAdmin(string $username, string $password): bool
    {
        $config         = config('Nica');
        $storedPassword = (string) $config->adminPassword;

        $passwordOk = str_starts_with($storedPassword, '$2y$') || str_starts_with($storedPassword, '$argon2')
            ? password_verify($password, $storedPassword)
            : ($storedPassword !== '' && hash_equals($storedPassword, $password));

        $userOk = $config->adminUser !== '' && hash_equals((string) $config->adminUser, $username);

        return $userOk && $passwordOk;
    }

    private function startSession(string $displayName, string $rol, ?string $uid, string $email): ResponseInterface
    {
        session()->regenerate();
        session()->set([
            'nica_admin'      => true,
            'nica_admin_user' => $email,
            'nica_admin_name' => $displayName,
            'nica_admin_role' => $rol,
            'nica_admin_uid'  => $uid,
            'nica_admin_time' => time(),
        ]);

        return redirect()->to(site_url('panel'));
    }

    /**
     * Inicia sesión contra Firebase Authentication (Identity Toolkit REST).
     *
     * @return array{uid: string, nombre: string}|null
     */
    private function firebaseSignIn(string $email, string $password): ?array
    {
        $apiKey = (string) config('Nica')->webApiKey;
        if ($apiKey === '' || $email === '' || $password === '') {
            return null;
        }

        try {
            $client   = service('curlrequest', ['timeout' => 15]);
            $response = $client->post(
                'https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=' . rawurlencode($apiKey),
                [
                    'headers'     => ['Content-Type' => 'application/json'],
                    'body'        => json_encode([
                        'email'             => $email,
                        'password'          => $password,
                        'returnSecureToken' => true,
                    ], JSON_UNESCAPED_UNICODE),
                    'http_errors' => false,
                ]
            );

            if ($response->getStatusCode() !== 200) {
                return null;
            }

            $data = json_decode((string) $response->getBody(), true);
            if (! is_array($data) || empty($data['localId'])) {
                return null;
            }

            return [
                'uid'    => (string) $data['localId'],
                'nombre' => trim((string) ($data['displayName'] ?? '')),
            ];
        } catch (Throwable) {
            return null;
        }
    }

    /**
     * Lee el rol del perfil del usuario en Firestore. Sin perfil o sin rol
     * válido se asume USUARIO (sin acceso al panel).
     */
    private function roleFromFirestore(string $uid): string
    {
        try {
            $repository = ResourceManager::repository('usuarios');
            if ($repository instanceof FirestoreRepository) {
                $snapshot = $repository->find($uid);
                $rol      = trim((string) ($snapshot['data']['rol'] ?? ''));

                if (in_array($rol, PanelPermissions::roles(), true)) {
                    return $rol;
                }
            }
        } catch (Throwable) {
            // Si Firestore falla, se niega el acceso (mínimo privilegio).
        }

        return PanelPermissions::ROLE_USER;
    }
}
