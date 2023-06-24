CREATE DATABASE securities;

\c securities;

CREATE TABLE securities(
  id uuid DEFAULT gen_random_uuid(),
  secid text NOT NULL,
  regnumber text NOT NULL,
  name text NOT NULL,
  emitent_title text NOT NULL
);

ALTER TABLE
  securities
ADD
  CONSTRAINT pk_security PRIMARY KEY (id);

CREATE TABLE histories(
  id uuid DEFAULT gen_random_uuid(),
  secid text NOT NULL,
  tradedate text NOT NULL,
  numtrades decimal NOT NULL,
  open decimal,
  close decimal
);  

ALTER TABLE
  histories
ADD
  CONSTRAINT pk_histories PRIMARY KEY (id);