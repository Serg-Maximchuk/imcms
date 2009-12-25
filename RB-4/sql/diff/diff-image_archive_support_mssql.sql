CREATE TABLE archive_images (
    id int identity NOT NULL, 
    image_nm nvarchar(255) NOT NULL DEFAULT '', 
    format smallint NOT NULL,
    width integer NOT NULL, 
    height integer NOT NULL, 
    file_size integer NOT NULL,
    uploaded_by nvarchar(130) NOT NULL DEFAULT '', 
    users_id integer NOT NULL, 
    status smallint NOT NULL DEFAULT 0, 
    created_dt datetime NOT NULL DEFAULT GETDATE(),
    updated_dt datetime NOT NULL DEFAULT GETDATE(), 
    license_dt datetime, 
    license_end_dt datetime, 
    publish_dt datetime, 
    archive_dt datetime, 
    publish_end_dt datetime, 
    CONSTRAINT archive_images_pk PRIMARY KEY (id), 
    CONSTRAINT archive_images_users_id_fk FOREIGN KEY (users_id) REFERENCES users(user_id)
);

CREATE TABLE archive_exif (
    image_id int NOT NULL, 
    exif_type smallint NOT NULL DEFAULT 0,
    resolution integer NOT NULL, 
    description nvarchar(255) NOT NULL DEFAULT '', 
    artist nvarchar(255) NOT NULL DEFAULT '', 
    copyright nvarchar(255) NOT NULL DEFAULT '', 
    created_dt datetime NOT NULL DEFAULT GETDATE(), 
    updated_dt datetime NOT NULL DEFAULT GETDATE(), 
    CONSTRAINT archive_exif_pk PRIMARY KEY (image_id, exif_type), 
    CONSTRAINT archive_image_id_fk FOREIGN KEY (image_id) REFERENCES archive_images(id)
);

CREATE TABLE archive_keywords (
    id int identity, 
    keyword_nm nvarchar(50) NOT NULL, 
    created_dt datetime NOT NULL DEFAULT GETDATE(), 
    CONSTRAINT archive_keywords_pk PRIMARY KEY (id)
);

CREATE TABLE archive_image_keywords (
    image_id int NOT NULL, 
    keyword_id int NOT NULL, 
    created_dt datetime NOT NULL DEFAULT GETDATE(), 
    CONSTRAINT archive_image_keywords_pk PRIMARY KEY (image_id, keyword_id), 
    CONSTRAINT archive_image_keywords_image_id_fk FOREIGN KEY (image_id) REFERENCES archive_images(id), 
    CONSTRAINT archive_image_keywords_keyword_id_fk FOREIGN KEY (keyword_id) REFERENCES archive_keywords(id)
);

CREATE TABLE category_roles (
    category_id integer NOT NULL, 
    role_id integer NOT NULL, 
    created_dt datetime NOT NULL DEFAULT GETDATE(), 
    CONSTRAINT category_roles_pk PRIMARY KEY (category_id, role_id), 
    CONSTRAINT category_roles_category_id_fk FOREIGN KEY (category_id) REFERENCES categories(category_id), 
    CONSTRAINT category_roles_role_id_fk FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE image_categories (
    image_id int NOT NULL, 
    category_id integer NOT NULL, 
    created_dt datetime NOT NULL DEFAULT GETDATE(), 
    CONSTRAINT image_categories_pk PRIMARY KEY (image_id, category_id), 
    CONSTRAINT image_categories_image_id_fk FOREIGN KEY (image_id) REFERENCES archive_images(id), 
    CONSTRAINT image_categories_category_id_fk FOREIGN KEY (category_id) REFERENCES categories(category_id)
);

CREATE TABLE archive_libraries (
    id int identity NOT NULL , 
    library_nm nvarchar(120) NOT NULL, 
    folder_nm nvarchar(255) NOT NULL, 
    filepath nvarchar(255), 
    library_type smallint NOT NULL DEFAULT 0, 
    created_dt datetime NOT NULL DEFAULT GETDATE(), 
    updated_dt datetime NOT NULL DEFAULT GETDATE(), 
    CONSTRAINT archive_libraries_pk PRIMARY KEY (id)--, 
    --CONSTRAINT archive_libraries_folder_nm_filepath_unq UNIQUE (folder_nm, filepath)
);

CREATE TABLE archive_library_roles (
    library_id integer NOT NULL, 
    role_id integer NOT NULL, 
    permissions integer NOT NULL, 
    created_dt datetime NOT NULL DEFAULT GETDATE(), 
    updated_dt datetime NOT NULL DEFAULT GETDATE(), 
    CONSTRAINT archive_library_roles_pk PRIMARY KEY (library_id, role_id), 
    CONSTRAINT archive_library_roles_library_id_fk FOREIGN KEY (library_id) REFERENCES archive_libraries(id), 
    CONSTRAINT archive_library_roles_role_id_fk FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

ALTER TABLE users ADD remember_cd varchar(40);
-- ALTER TABLE users ADD CONSTRAINT users_remember_cd_unq UNIQUE (remember_cd);

ALTER TABLE images ADD archive_image_id bigint;
ALTER TABLE images_history ADD archive_image_id bigint;

ALTER TABLE category_types ADD is_image_archive tinyint NOT NULL DEFAULT 0;
