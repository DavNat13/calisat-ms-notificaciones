-- Directorio de destinatarios: sin filas aqui, OrdenListener/EnvioListener
-- loguean "correo omitido". ON CONFLICT evita duplicar en cada arranque.
-- Anade los tuyos con POST /api/v1/destinatarios (upsert por azure_sub).
INSERT INTO destinatario (id, azure_sub, email, nombre, rol, activo, fecha_creacion, fecha_actualizacion)
VALUES
  (gen_random_uuid(), '00000000-0000-0000-0000-000000000001',
   'cliente.demo@calisat.com', 'Cliente Demo', 'cliente', true, now(), now()),
  (gen_random_uuid(), '00000000-0000-0000-0000-000000000002',
   'admin.demo@calisat.com', 'Admin Demo', 'administrador', true, now(), now())
ON CONFLICT (azure_sub) DO NOTHING;
