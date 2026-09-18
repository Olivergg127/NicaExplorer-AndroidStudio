<?php

namespace App\Libraries;

/**
 * Punto de acceso a los repositorios de Firestore declarados en NicaResources.
 */
class ResourceManager
{
    /** @var array<string, FirestoreRepository> */
    private static array $repositories = [];

    /**
     * @return array<string, array<string, mixed>>
     */
    public static function all(): array
    {
        return config('NicaResources')->all();
    }

    /**
     * @return array<string, mixed>|null
     */
    public static function definition(string $key): ?array
    {
        return config('NicaResources')->get($key);
    }

    public static function repository(string $key): ?FirestoreRepository
    {
        if (isset(self::$repositories[$key])) {
            return self::$repositories[$key];
        }

        $definition = self::definition($key);
        if ($definition === null) {
            return null;
        }

        return self::$repositories[$key] = new FirestoreRepository(FirebaseFactory::instance()->database(), $definition);
    }

    public static function exists(string $key): bool
    {
        return self::definition($key) !== null;
    }
}
