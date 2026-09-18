<?php

namespace App\Controllers\Panel;

use App\Controllers\BaseController;
use CodeIgniter\HTTP\ResponseInterface;

/**
 * Autenticación sencilla del panel web (usuario único configurado en .env).
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
        $config   = config('Nica');
        $username = trim((string) $this->request->getPost('username'));
        $password = (string) $this->request->getPost('password');

        $storedPassword = (string) $config->adminPassword;
        $passwordOk     = str_starts_with($storedPassword, '$2y$') || str_starts_with($storedPassword, '$argon2')
            ? password_verify($password, $storedPassword)
            : hash_equals($storedPassword, $password);

        $userOk = $config->adminUser !== '' && hash_equals((string) $config->adminUser, $username);

        if ($userOk && $passwordOk) {
            session()->regenerate();
            session()->set([
                'nica_admin'      => true,
                'nica_admin_user' => $username,
                'nica_admin_time' => time(),
            ]);

            return redirect()->to(site_url('panel'));
        }

        return redirect()->back()->with('error', 'Usuario o contraseña incorrectos.');
    }

    public function logout(): ResponseInterface
    {
        session()->destroy();

        return redirect()->to(site_url('panel/login'));
    }
}
