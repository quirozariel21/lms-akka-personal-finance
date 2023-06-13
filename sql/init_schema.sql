--
/*drop database if exists latest;
create database latest;*/
// TODO rename to finance-db
CREATE DATABASE db_finance ENCODING 'UTF8';

DROP TABLE IF EXISTS public.users;
CREATE TABLE public.users (
	id SERIAL PRIMARY KEY,
	firstname VARCHAR(50) NOT NULL,
	lastname VARCHAR(50) NOT NULL,
	email VARCHAR(50) UNIQUE NOT NULL
);

DROP TABLE IF EXISTS public.category;
CREATE TABLE public.category(
	id SERIAL PRIMARY KEY,
	"name" VARCHAR(100) UNIQUE NOT NULL,
    "description" VARCHAR(150),
    is_active BOOLEAN NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    subcategory_id INTEGER NOT NULL
);

DROP TABLE IF EXISTS public.tx_personal_finance;
CREATE TABLE public.tx_personal_finance(
	id SERIAL PRIMARY KEY,
	"year" INTEGER NOT NULL,
	"month" VARCHAR(15) NOT NULL,
	created_at TIMESTAMP DEFAULT NOW(),
	created_by VARCHAR(50) NOT NULL
);

DROP TABLE IF EXISTS public.tx_incomes;
CREATE TABLE public.tx_incomes(
	id SERIAL PRIMARY KEY,
	personal_finance_id INT NOT NULL,
	"name" VARCHAR(20) NOT NULL,
	currency VARCHAR(5) NOT NULL,
	note VARCHAR(50),
	created_at TIMESTAMP DEFAULT NOW(),
	created_by VARCHAR(50) NOT NULL,
	CONSTRAINT fk_personal_finance_id
	    FOREIGN KEY (personal_finance_id)
	        REFERENCES tx_personal_finance (id)
);

DROP TABLE IF EXISTS public.tx_expense;
CREATE TABLE public.tx_expense(
	id SERIAL PRIMARY KEY,
	category_id INTEGER NOT NULL,
	subcategory_id INTEGER NOT NULL,
	"note" VARCHAR(50),
	amount NUMERIC(5,2) NOT NULL,
	currency VARCHAR(5) NOT NULL,
	expensed_date DATE NOT NULL,
	created_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(50) NOT NULL,
	CONSTRAINT fk_category_id FOREIGN KEY(category_id) REFERENCES categories(id),
	CONSTRAINT fk_subcategory_id FOREIGN KEY(subcategory_id) REFERENCES categories(id)
);

INSERT INTO categories(name, description)
VALUES ('SERVICIOS BASICOS', ''),
('VIVIENDA', ''),
('DEUDAS', ''),
('AUTOMOVIL', ''),
('SEGUROS', ''),
('EDUCACION', ''),
('VIAJES Y PASEOS', ''),
('ALIMENTOS', ''),
('SALUD', ''),
('MASCOTAS', ''),
('CUMPLE Y DIAS ESPECIALES', '');