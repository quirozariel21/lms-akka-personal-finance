--
-- Create tables

DROP TABLE IF EXISTS users;
CREATE TABLE users (
	id SERIAL PRIMARY KEY,
	firstname VARCHAR(50) NOT NULL,
	lastname VARCHAR(50) NOT NULL,
	username VARCHAR(50) UNIQUE NOT NULL,
	email VARCHAR(50) UNIQUE NOT NULL,
	created_at TIMESTAMP DEFAULT NOW()
);

DROP TABLE IF EXISTS category;
CREATE TABLE category(
	id SERIAL PRIMARY KEY,
	"name" VARCHAR(100) NOT NULL,
    "description" VARCHAR(150),
    is_active BOOLEAN NOT NULL,
    parent_id INTEGER,
    created_at TIMESTAMP DEFAULT NOW()
);

DROP TABLE IF EXISTS personal_finance;
CREATE TABLE public.personal_finance(
	id SERIAL PRIMARY KEY,
	"year" INTEGER NOT NULL,
	"month" VARCHAR(15) NOT NULL,
	is_active BOOLEAN NOT NULL,
	created_at TIMESTAMP DEFAULT NOW(),
	created_by VARCHAR(50) NOT NULL
);

DROP TABLE IF EXISTS income;
CREATE TABLE public.income(
	id SERIAL PRIMARY KEY,
	personal_finance_id INT NOT NULL,
	"name" VARCHAR(20) NOT NULL,
	currency VARCHAR(5) NOT NULL,
	amount NUMERIC(7,2) NOT NULL DEFAULT 0,
	note VARCHAR(50),
	is_active BOOLEAN NOT NULL,
	created_at TIMESTAMP DEFAULT NOW(),
	created_by VARCHAR(50) NOT NULL,
	CONSTRAINT fk_personal_finance_id
	    FOREIGN KEY (personal_finance_id)
	        REFERENCES personal_finance (id)
);

DROP TABLE IF EXISTS public.expense;
CREATE TABLE public.expense(
	id SERIAL PRIMARY KEY,
	category_id INTEGER NOT NULL,
	subcategory_id INTEGER NOT NULL,
	"note" VARCHAR(50),
	amount NUMERIC(7,2) NOT NULL,
	currency VARCHAR(5) NOT NULL,
	expensed_date DATE NOT NULL,
	is_active BOOLEAN NOT NULL,
	personal_finance_id INTEGER NOT NULL,
	created_at TIMESTAMP DEFAULT NOW(),
    created_by VARCHAR(50) NOT NULL,
	CONSTRAINT fk_category_id FOREIGN KEY(category_id) REFERENCES category(id),
	CONSTRAINT fk_subcategory_id FOREIGN KEY(subcategory_id) REFERENCES category(id),
    CONSTRAINT fk_personal_finance_id FOREIGN KEY (personal_finance_id) REFERENCES personal_finance (id)
);
