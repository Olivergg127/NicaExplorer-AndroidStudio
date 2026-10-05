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
            'apkUrl'  => 'https://github.com/Olivergg127/NicaExplorer-AndroidStudio/releases/latest/download/NicaExplorer.apk',
            'repoUrl' => 'https://github.com/Olivergg127/NicaExplorer-AndroidStudio',
            'version' => '1.1.2',
            'year'    => date('Y'),
        ]);
    }
}
