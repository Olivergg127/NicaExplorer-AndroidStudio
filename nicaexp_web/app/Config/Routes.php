<?php

use CodeIgniter\Router\RouteCollection;

/** @var RouteCollection $routes */

$routes->get('/', static fn () => redirect()->to(site_url('panel')));

// ---------------------------------------------------------------------
// API REST v1 (protegida con API key: header X-API-KEY)
// ---------------------------------------------------------------------
$routes->group('api/v1', ['filter' => 'apikey'], static function (RouteCollection $routes): void {
    $routes->get('health', 'Api\Health::index');
    $routes->get('version', 'Api\Catalog::version');

    $routes->get('(:segment)', 'Api\Resources::index/$1');
    $routes->post('(:segment)', 'Api\Resources::create/$1');
    $routes->get('(:segment)/(:segment)', 'Api\Resources::show/$1/$2');
    $routes->put('(:segment)/(:segment)', 'Api\Resources::update/$1/$2');
    $routes->patch('(:segment)/(:segment)', 'Api\Resources::update/$1/$2');
    $routes->delete('(:segment)/(:segment)', 'Api\Resources::delete/$1/$2');
});

// ---------------------------------------------------------------------
// Panel web
// ---------------------------------------------------------------------
$routes->get('panel/login', 'Panel\Auth::loginForm');
$routes->post('panel/login', 'Panel\Auth::attempt', ['filter' => 'csrf']);
$routes->get('panel/logout', 'Panel\Auth::logout');

$routes->group('panel', ['filter' => 'panelauth,panelcan'], static function (RouteCollection $routes): void {
    $routes->get('', 'Panel\Dashboard::index');

    $routes->post('upload', 'Panel\Resources::upload', ['filter' => 'csrf']);
    $routes->get('(:segment)/data', 'Panel\Resources::data/$1');
    $routes->post('(:segment)/save', 'Panel\Resources::save/$1', ['filter' => 'csrf']);
    $routes->post('(:segment)/delete', 'Panel\Resources::delete/$1', ['filter' => 'csrf']);
    $routes->get('(:segment)', 'Panel\Resources::index/$1');
});
