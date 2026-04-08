ALTER TABLE prices
    ALTER COLUMN amount TYPE NUMERIC(19, 6);

UPDATE prices p
SET amount = (p.amount_minor::NUMERIC / POWER(10, c.fraction_digits))
FROM currencies c
WHERE c.code = p.currency;
