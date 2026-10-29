package pe.edu.upeu.autoandes.security;

import java.util.*;

public final class PermisosPorRol {

    private static final Map<String, Set<Permiso>> MATRIZ = new HashMap<>();

    static {
        MATRIZ.put("ROLE_ADMIN", EnumSet.allOf(Permiso.class));
        MATRIZ.put("ADMIN", EnumSet.allOf(Permiso.class));

        Set<Permiso> asesor = EnumSet.of(
                Permiso.SERVICIO_LEER,
                Permiso.VEHICULO_LEER,
                Permiso.VEHICULO_ESCRIBIR,
                Permiso.ORDEN_LEER,
                Permiso.ORDEN_REGISTRAR
        );
        MATRIZ.put("ROLE_ASESOR", asesor);
        MATRIZ.put("ASESOR", asesor);

        Set<Permiso> jefeTaller = EnumSet.of(
                Permiso.SERVICIO_LEER,
                Permiso.SERVICIO_ESCRIBIR,
                Permiso.VEHICULO_LEER,
                Permiso.ORDEN_LEER,
                Permiso.ORDEN_ANULAR,
                Permiso.REPORTE_VER
        );
        MATRIZ.put("ROLE_JEFE_TALLER", jefeTaller);
        MATRIZ.put("JEFE_TALLER", jefeTaller);
    }

    private PermisosPorRol() {}

    public static Set<Permiso> obtenerPermisos(String nombreRol) {
        if (nombreRol == null) return Collections.emptySet();
        return MATRIZ.getOrDefault(nombreRol.toUpperCase(), Collections.emptySet());
    }

    public static Set<Permiso> obtenerPermisosCombinados(Collection<String> roles) {
        Set<Permiso> resultado = EnumSet.noneOf(Permiso.class);
        if (roles != null) {
            for (String r : roles) {
                resultado.addAll(obtenerPermisos(r));
            }
        }
        return resultado;
    }
}
