-- Crear tabla para filtros guardados por usuario
CREATE TABLE IF NOT EXISTS filtros_guardados (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    filtros_json TEXT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Índices y constraints
    INDEX idx_usuario_id (usuario_id),
    INDEX idx_nombre (nombre),
    UNIQUE KEY unique_user_filter_name (usuario_id, nombre),
    
    -- Foreign key constraint (asumiendo que existe tabla usuario)
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

-- Comentarios para documentación
ALTER TABLE filtros_guardados COMMENT = 'Tabla para almacenar filtros de búsqueda guardados por usuario';
ALTER TABLE filtros_guardados MODIFY COLUMN nombre VARCHAR(100) NOT NULL COMMENT 'Nombre descriptivo del filtro dado por el usuario';
ALTER TABLE filtros_guardados MODIFY COLUMN filtros_json TEXT NOT NULL COMMENT 'JSON con los filtros: {categoria, fechaDesde, fechaHasta, eliminado}';
ALTER TABLE filtros_guardados MODIFY COLUMN fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Fecha y hora de creación del filtro';
ALTER TABLE filtros_guardados MODIFY COLUMN fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Fecha y hora de última actualización';

-- Insertar algunos filtros de ejemplo (opcional, para testing)
-- INSERT INTO filtros_guardados (usuario_id, nombre, filtros_json) VALUES 
-- (1, 'Ropa de este mes', '{"categoria": "ROPA", "fechaDesde": "2025-10-01", "fechaHasta": "2025-10-31", "eliminado": null}'),
-- (1, 'Solo eliminados', '{"categoria": "", "fechaDesde": "", "fechaHasta": "", "eliminado": true}'),
-- (1, 'Comida activa', '{"categoria": "COMIDA", "fechaDesde": "", "fechaHasta": "", "eliminado": false}');