INSERT INTO tb_partner (id, name, credit_limit)
VALUES
(gen_random_uuid(), 'Partner Alpha', 10000.00, '2017-11-29 13:50:05.878000', '2017-11-29 13:50:05.878000'),
(gen_random_uuid(), 'Partner Beta', 5000.00, '2017-11-07 15:09:01.674000', '2017-11-07 15:09:01.674000'),
(gen_random_uuid(), 'Partner Gamma', 20000.00, '2017-11-07 15:09:01.674000', '2017-11-07 15:09:01.674000');

INSERT INTO tb_order (id, partner_id, total_amount, status)
SELECT gen_random_uuid(), id, 1500.00, 'PENDENTE', '2017-11-07 15:09:01.674000', '2017-11-07 15:09:01.674000' FROM tb_partner LIMIT 1;

INSERT INTO tb_order (id, partner_id, total_amount, status)
SELECT gen_random_uuid(), id, 750.00, 'ENVIADO', '2017-11-07 15:09:01.674000', '2017-11-07 15:09:01.674000' FROM tb_partner OFFSET 1 LIMIT 1;

INSERT INTO tb_order (id, partner_id, total_amount, status)
SELECT gen_random_uuid(), id, 3400.00, 'ENTREGUE', '2017-11-07 15:09:01.674000', '2017-11-07 15:09:01.674000' FROM tb_partner OFFSET 1 LIMIT 1;

INSERT INTO tb_order_item (id, order_id, product_id, quantity, unit_price)
SELECT gen_random_uuid(), id, gen_random_uuid(), 1, 1500.00, '2017-11-07 15:09:01.674000', '2017-11-07 15:09:01.674000' FROM tb_order LIMIT 1;

INSERT INTO tb_order_item (id, order_id, product_id, quantity, unit_price)
SELECT gen_random_uuid(), id, gen_random_uuid(), 1, 750.00, '2017-11-29 15:15:13.636000', '2017-11-29 15:15:13.636000' FROM tb_order OFFSET 1 LIMIT 1;

INSERT INTO tb_order_item (id, order_id, product_id, quantity, unit_price)
SELECT gen_random_uuid(), id, gen_random_uuid(), 1, 3400.00, '2017-11-29 13:50:05.878000' '2017-11-29 13:50:05.878000' FROM tb_order OFFSET 2 LIMIT 1;