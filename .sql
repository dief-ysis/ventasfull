-- script_creacion_tablas.sql

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    address VARCHAR(255),
    phone VARCHAR(20),
    enabled BOOLEAN DEFAULT TRUE,
    role VARCHAR(50) NOT NULL
);

CREATE TABLE inventario (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    precio DECIMAL(10, 2) NOT NULL CHECK (precio >= 0),
    stock INTEGER NOT NULL CHECK (stock >= 0),
    categoria VARCHAR(50),
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    ultima_actualizacion TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE carrito (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE TABLE carrito_items (
    id BIGSERIAL PRIMARY KEY,
    carrito_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INTEGER NOT NULL CHECK (cantidad > 0),
    precio_unitario DECIMAL(10, 2) NOT NULL, 

    FOREIGN KEY (carrito_id) REFERENCES carrito (id) ON DELETE CASCADE,

    FOREIGN KEY (producto_id) REFERENCES inventario (id) ON DELETE RESTRICT, 
    UNIQUE (carrito_id, producto_id) 
);

CREATE INDEX idx_usuarios_username ON usuarios (username);
CREATE INDEX idx_inventario_nombre ON inventario (nombre);
CREATE INDEX idx_carrito_user_id ON carrito (user_id);
CREATE INDEX idx_carrito_items_carrito_id ON carrito_items (carrito_id);
CREATE INDEX idx_carrito_items_producto_id ON carrito_items (producto_id);