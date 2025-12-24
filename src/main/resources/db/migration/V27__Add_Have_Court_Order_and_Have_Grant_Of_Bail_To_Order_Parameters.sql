ALTER TABLE order_parameters
    ADD COLUMN have_court_order BOOLEAN;

ALTER TABLE order_parameters
    ADD COLUMN have_grant_of_bail BOOLEAN;