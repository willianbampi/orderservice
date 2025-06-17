INSERT INTO partner (id, name, credit_limit)
VALUES
(gen_random_uuid(), 'Partner Alpha', 10000.00),
(gen_random_uuid(), 'Partner Beta', 5000.00),
(gen_random_uuid(), 'Partner Gamma', 20000.00);

INSERT INTO order (id, partner_id, total_amount, status)
SELECT gen_random_uuid(), id, 1500.00, 'PENDENTE' FROM partner LIMIT 1;

INSERT INTO order (id, partner_id, total_amount, status)
SELECT gen_random_uuid(), id, 750.00, 'ENVIADO' FROM partner OFFSET 1 LIMIT 1;

INSERT INTO order (id, partner_id, total_amount, status)
SELECT gen_random_uuid(), id, 3400.00, 'ENTREGUE' FROM partner OFFSET 1 LIMIT 1;

INSERT INTO order_item (id, order_id, product_id, quantity, unit_price)
SELECT gen_random_uuid(), id, gen_random_uuid(), 1, 1500.00 FROM order LIMIT 1;

INSERT INTO order_item (id, order_id, product_id, quantity, unit_price)
SELECT gen_random_uuid(), id, gen_random_uuid(), 1, 750.00 FROM order OFFSET 1 LIMIT 1;

INSERT INTO order_item (id, order_id, product_id, quantity, unit_price)
SELECT gen_random_uuid(), id, gen_random_uuid(), 1, 3400.00 FROM order OFFSET 2 LIMIT 1;