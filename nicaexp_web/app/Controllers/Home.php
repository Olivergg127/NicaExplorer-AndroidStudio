<?php

namespace App\Controllers;

class Home extends BaseController
{
    /**
     * Landing pública de NicaExplore, servida en la raíz del dominio.
     */
    public function index(): string
    {
        return view('landing', [
            'apkUrl'  => base_url('builds/NicaExplorer.apk'),
            'repoUrl' => 'https://github.com/Olivergg127/NicaExplorer-AndroidStudio',
            'version' => '1.1.3',
            'year'    => date('Y'),
        ]);
    }
}
