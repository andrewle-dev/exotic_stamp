-- Partner promotional banner for mobile Home carousel (URL string only; no binary in DB).
ALTER TABLE partners
    ADD COLUMN banner_image_url VARCHAR(255);
