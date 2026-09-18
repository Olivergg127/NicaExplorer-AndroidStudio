<?php

namespace App\Filters;

use CodeIgniter\Filters\FilterInterface;
use CodeIgniter\HTTP\RequestInterface;
use CodeIgniter\HTTP\ResponseInterface;

/**
 * Exige la API key configurada (Nica.apiKey) en los endpoints /api/v1/*.
 *
 * Se acepta por header "X-API-KEY" o por query string "?api_key=".
 */
class ApiKeyFilter implements FilterInterface
{
    /**
     * @param list<string>|null $arguments
     *
     * @return ResponseInterface|null
     */
    public function before(RequestInterface $request, $arguments = null)
    {
        $expected = (string) config('Nica')->apiKey;

        $provided = trim($request->getHeaderLine('X-API-KEY'));
        if ($provided === '') {
            $provided = trim((string) $request->getGet('api_key'));
        }

        if ($expected === '' || $provided === '' || ! hash_equals($expected, $provided)) {
            return service('response')
                ->setStatusCode(401)
                ->setJSON([
                    'error'   => 'No autorizado',
                    'message' => 'API key inválida o ausente.',
                ]);
        }

        return null;
    }

    /**
     * @param list<string>|null $arguments
     */
    public function after(RequestInterface $request, ResponseInterface $response, $arguments = null)
    {
    }
}
