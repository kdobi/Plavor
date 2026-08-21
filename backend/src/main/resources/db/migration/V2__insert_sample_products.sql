INSERT INTO categories (id, name, slug, display_order, active)
VALUES
    (1, 'Tops', 'tops', 1, TRUE),
    (2, 'Outerwear', 'outerwear', 2, TRUE),
    (3, 'Bags', 'bags', 3, TRUE);

INSERT INTO products (
    id,
    category_id,
    name,
    slug,
    description,
    price,
    stock_quantity,
    status
)
VALUES
    (
        1,
        1,
        'Minimal Cotton T-Shirt',
        'minimal-cotton-t-shirt',
        'A clean everyday cotton T-shirt with a relaxed silhouette.',
        29000,
        120,
        'ACTIVE'
    ),
    (
        2,
        2,
        'Relaxed Zip Hoodie',
        'relaxed-zip-hoodie',
        'A medium-weight hoodie designed for daily layering.',
        69000,
        45,
        'ACTIVE'
    ),
    (
        3,
        2,
        'Daily Denim Jacket',
        'daily-denim-jacket',
        'A structured denim jacket with a soft worn-in finish.',
        89000,
        0,
        'SOLD_OUT'
    ),
    (
        4,
        3,
        'Hidden Sample Tote',
        'hidden-sample-tote',
        'A hidden product used to verify public catalog filtering.',
        39000,
        20,
        'HIDDEN'
    );

INSERT INTO product_images (
    id,
    product_id,
    image_url,
    alt_text,
    display_order,
    thumbnail
)
VALUES
    (
        1,
        1,
        'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab',
        'Minimal Cotton T-Shirt',
        1,
        TRUE
    ),
    (
        2,
        2,
        'https://images.unsplash.com/photo-1556821840-3a63f95609a7',
        'Relaxed Zip Hoodie',
        1,
        TRUE
    ),
    (
        3,
        3,
        'https://images.unsplash.com/photo-1543076447-215ad9ba6923',
        'Daily Denim Jacket',
        1,
        TRUE
    ),
    (
        4,
        4,
        'https://images.unsplash.com/photo-1590874103328-eac38a683ce7',
        'Hidden Sample Tote',
        1,
        TRUE
    );

SELECT setval(pg_get_serial_sequence('categories', 'id'), 3, TRUE);
SELECT setval(pg_get_serial_sequence('products', 'id'), 4, TRUE);
SELECT setval(pg_get_serial_sequence('product_images', 'id'), 4, TRUE);
