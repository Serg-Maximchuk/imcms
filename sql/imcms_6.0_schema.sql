﻿-- ============================================================================
-- Tables
-- ============================================================================

--
-- Table users
--

CREATE TABLE users (
  user_id int(11) NOT NULL auto_increment,
  login_name varchar(128) NOT NULL,
  login_password varchar(15) NOT NULL,
  first_name varchar(64) NOT NULL,
  last_name varchar(64) NOT NULL,
  title varchar(64) NOT NULL,
  company varchar(64) NOT NULL,
  address varchar(128) NOT NULL,
  city varchar(64) NOT NULL,
  zip varchar(64) NOT NULL,
  country varchar(64) NOT NULL,
  county_council varchar(128) NOT NULL,
  email varchar(128) NOT NULL,
  external int(11) NOT NULL,
  active int(11) NOT NULL default '1',
  create_date datetime NOT NULL,
  language varchar(3) NOT NULL,
  session_id varchar(128) default NULL,
  PRIMARY KEY  (user_id),
  UNIQUE KEY UQ__users__login_name (login_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ### META_ID 
--
-- Table meta
--
CREATE TABLE meta (
  meta_id int(11) NOT NULL auto_increment,
  doc_type int(11) NOT NULL,
  owner_id int(11) NOT NULL,
  permissions int(11) NOT NULL,
  shared int(11) NOT NULL,
  show_meta int(11) NOT NULL,
  lang_prefix varchar(3) NOT NULL,
  date_created datetime NOT NULL,
  date_modified datetime NOT NULL,
  disable_search int(11) NOT NULL,
  target varchar(10) NOT NULL,
  activate int(11) NOT NULL,
  archived_datetime datetime default NULL,
  publisher_id int(11) default NULL,
  status int(11) NOT NULL,
  publication_start_datetime datetime default NULL,
  publication_end_datetime datetime default NULL,
  missing_i18n_show_rule varchar(32) default 'DO_NOT_SHOW',
  PRIMARY KEY  (meta_id),
  CONSTRAINT meta_FK_owner_id_users FOREIGN KEY (owner_id) REFERENCES users (user_id),
  CONSTRAINT meta_FK_publisher_id_users FOREIGN KEY (publisher_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table category_types
--

CREATE TABLE category_types (
  category_type_id int(11) NOT NULL auto_increment,
  name varchar(128) NOT NULL,
  max_choices int(11) NOT NULL default '0',
  inherited tinyint(1) NOT NULL,
  PRIMARY KEY  (category_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table categories
--

CREATE TABLE categories (
  category_id int(11) NOT NULL auto_increment,
  category_type_id int(11) NOT NULL,
  name varchar(128) NOT NULL,
  description varchar(500) default NULL,
  image varchar(255) NOT NULL,
  PRIMARY KEY  (category_id),
  KEY categories_FK_category_type_id_category_types (category_type_id),
  CONSTRAINT categories_FK_category_type_id_category_types FOREIGN KEY (category_type_id) REFERENCES category_types (category_type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- Table database_version
--

CREATE TABLE database_version (
  major int(11) NOT NULL,
  minor int(11) NOT NULL,
  PRIMARY KEY  (major,minor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table doc_permissions
--

CREATE TABLE doc_permissions (
  permission_id int(11) NOT NULL,
  doc_type int(11) NOT NULL,
  lang_prefix varchar(3) NOT NULL,
  description varchar(50) NOT NULL,
  PRIMARY KEY  (permission_id,doc_type,lang_prefix)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table doc_types
--

CREATE TABLE doc_types (
  doc_type int(11) NOT NULL,
  lang_prefix varchar(3) NOT NULL default 'swe',
  type varchar(50) default NULL,
  PRIMARY KEY  (doc_type,lang_prefix)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ### META_ID
--
-- Table document_categories
--
CREATE TABLE document_categories (
  meta_id int(11) NOT NULL,
  category_id int(11) NOT NULL,
  PRIMARY KEY  (meta_id,category_id),
  KEY document_categories_FK_category_id_categories (category_id),
  CONSTRAINT document_categories_FK_category_id_categories FOREIGN KEY (category_id) REFERENCES categories (category_id),
  CONSTRAINT document_categories_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table document_properties
--
CREATE TABLE document_properties (
  id int(11) NOT NULL auto_increment,
  meta_id int(11) NOT NULL,
  key_name varchar(255) NOT NULL,
  value varchar(255) NOT NULL,
  PRIMARY KEY  (id),
  UNIQUE KEY UQ_document_properties__meta_id__key_name (meta_id,key_name),
  CONSTRAINT document_properties_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table document_search_log
--

CREATE TABLE document_search_log (
  datetime datetime NOT NULL,
  term varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table fileupload_docs
--
CREATE TABLE fileupload_docs (
  meta_id int(11) NOT NULL,
  variant_name varchar(100) NOT NULL,
  filename varchar(255) NOT NULL,
  mime varchar(50) NOT NULL,
  created_as_image int(11) NOT NULL,
  default_variant tinyint(1) NOT NULL default '0',
  PRIMARY KEY  (meta_id,variant_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table frameset_docs
--
CREATE TABLE frameset_docs (
  meta_id int(11) NOT NULL,
  frame_set longtext,
  PRIMARY KEY  (meta_id),
  CONSTRAINT frameset_docs_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table i18n_languages
--

CREATE TABLE i18n_languages (
  language_id smallint NOT NULL auto_increment,
  code varchar(3) NOT NULL COMMENT 'Language code',
  name varchar(128) NOT NULL,
  native_name varchar(128) default NULL,
  is_default boolean NOT NULL default false COMMENT 'Default language flag for application. Only one language can be set as default.',
  is_enabled boolean NOT NULL default true COMMENT 'Language status for application. Reserved for future use.',
  PRIMARY KEY (language_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table keywords
--
CREATE TABLE keywords (
  keyword_id int NOT NULL auto_increment,
  meta_id int default NULL,          
  language_id smallint NULL, 
  value varchar(128) NOT NULL,
  PRIMARY KEY (keyword_id),
  CONSTRAINT fk__keywords__meta FOREIGN KEY(meta_id) REFERENCES meta (meta_id) ON DELETE CASCADE,
  CONSTRAINT fk__keywords__i18n_languages FOREIGN KEY(language_id) REFERENCES i18n_languages (language_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table i18n_meta
--
CREATE TABLE i18n_meta (
  i18n_meta_id int(11) NOT NULL auto_increment,
  language_id smallint(6) default NULL,
  meta_id int(11) default NULL,
  meta_enabled tinyint(1) NOT NULL default '0',
  meta_headline varchar(255) default NULL,
  meta_text varchar(1000) default NULL,
  meta_image varchar(255) default NULL,

  PRIMARY KEY (i18n_meta_id),
  UNIQUE INDEX ux__i18n_meta__meta_id__language_id(meta_id, language_id),
  FOREIGN KEY fk__i18n_meta__meta (meta_id) REFERENCES meta (meta_id) ON DELETE CASCADE,
  FOREIGN KEY fk__i18n_meta__language (language_id) REFERENCES i18n_languages (language_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table images
--
CREATE TABLE images (
  meta_id int(11) NULL,
  width int(11) NOT NULL,
  height int(11) NOT NULL,
  border int(11) NOT NULL,
  v_space int(11) NOT NULL,
  h_space int(11) NOT NULL,
  name int(11) NOT NULL,
  image_name varchar(40) NOT NULL default '',
  target varchar(15) NOT NULL,
  align varchar(15) NOT NULL,
  alt_text varchar(255) NOT NULL,
  low_scr varchar(255) NOT NULL,
  imgurl varchar(255) NOT NULL,
  linkurl varchar(255) NOT NULL,
  type int(11) NOT NULL,
  language_id smallint(6) NOT NULL,
  image_id int(11) NOT NULL auto_increment,
  PRIMARY KEY  (image_id),

  FOREIGN KEY fk__images__meta (meta_id) REFERENCES meta (meta_id) ON DELETE CASCADE,
  FOREIGN KEY fk__images__i18n_languages (language_id) REFERENCES i18n_languages (language_id),
  UNIQUE INDEX ux__images__meta_id__name__language_id (meta_id, name, language_id)

) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*
-- ### META_ID
--
-- Table images_history
--

CREATE TABLE images_history (
  meta_id int(11) NOT NULL,
  width int(11) NOT NULL,
  height int(11) NOT NULL,
  border int(11) NOT NULL,
  v_space int(11) NOT NULL,
  h_space int(11) NOT NULL,
  name int(11) NOT NULL,
  image_name varchar(40) NOT NULL,
  target varchar(15) NOT NULL,
  align varchar(15) NOT NULL,
  alt_text varchar(255) NOT NULL,
  low_scr varchar(255) NOT NULL,
  imgurl varchar(255) NOT NULL,
  linkurl varchar(255) NOT NULL,
  type int(11) NOT NULL,
  modified_datetime datetime NOT NULL,
  user_id int(11) NOT NULL,
  counter int(11) NOT NULL auto_increment,
  PRIMARY KEY  (counter),
  KEY images_history_FK_meta_id_meta (meta_id),
  KEY images_history_FK_user_id_users (user_id),
  CONSTRAINT images_history_FK_user_id_users FOREIGN KEY (user_id) REFERENCES users (user_id),
  CONSTRAINT images_history_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
*/

-- ### META_ID
--
-- Table includes
--
CREATE TABLE includes (
  id int auto_increment PRIMARY KEY,
  meta_id int NULL,
  include_id int NOT NULL,
  included_meta_id int NULL,

  UNIQUE INDEX ux__includes__meta_id__include_id(meta_id, include_id),
  FOREIGN KEY fk__includes__meta (meta_id) REFERENCES meta (meta_id) ON DELETE CASCADE,
  FOREIGN KEY fk__includes__included_meta (included_meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table ip_accesses
--

CREATE TABLE ip_accesses (
  ip_access_id int(11) NOT NULL auto_increment,
  user_id int(11) NOT NULL,
  ip_start decimal(18,0) NOT NULL,
  ip_end decimal(18,0) NOT NULL,
  PRIMARY KEY  (ip_access_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table lang_prefixes
--

CREATE TABLE lang_prefixes (
  lang_id int(11) NOT NULL,
  lang_prefix char(3) default NULL,
  PRIMARY KEY  (lang_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table languages
--

CREATE TABLE languages (
  lang_prefix varchar(3) NOT NULL,
  user_prefix varchar(3) NOT NULL,
  language varchar(30) default NULL,
  PRIMARY KEY  (lang_prefix,user_prefix)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table menus
--
CREATE TABLE menus (
  menu_id int(11) NOT NULL auto_increment,
  meta_id int(11) NOT NULL,
  menu_index int(11) NOT NULL,
  sort_order int(11) NOT NULL,
  PRIMARY KEY  (menu_id),
  UNIQUE KEY UQ_menus__meta_id__menu_index (meta_id,menu_index),
  CONSTRAINT menus_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table childs
--
CREATE TABLE childs (
  to_meta_id int(11) NOT NULL,
  manual_sort_order int(11) NOT NULL,
  tree_sort_index varchar(64) NOT NULL,
  menu_id int(11) NOT NULL,
  PRIMARY KEY  (to_meta_id,menu_id),
  KEY childs_FK_menu_id_menus (menu_id),
  CONSTRAINT childs_FK_menu_id_menus FOREIGN KEY (menu_id) REFERENCES menus (menu_id),
  CONSTRAINT childs_FK_to_meta_id_meta FOREIGN KEY (to_meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


/*
-- ### META_ID
--
-- Table menus_history
--
CREATE TABLE menus_history (
  menu_id int(11) NOT NULL,
  meta_id int(11) NOT NULL,
  menu_index int(11) NOT NULL,
  sort_order int(11) NOT NULL,
  modified_datetime datetime NOT NULL,
  user_id int(11) NOT NULL,
  PRIMARY KEY  (menu_id),
  KEY menus_history_FK_meta_id_meta (meta_id),
  CONSTRAINT menus_history_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table childs_history
--
CREATE TABLE childs_history (
  menu_id int(11) NOT NULL,
  to_meta_id int(11) NOT NULL,
  manual_sort_order int(11) NOT NULL,
  tree_sort_index varchar(64) NOT NULL,
  PRIMARY KEY  (menu_id,to_meta_id),
  KEY childs_history_FK_to_meta_id_meta (to_meta_id),
  CONSTRAINT childs_history_FK_menu_id_menus_history FOREIGN KEY (menu_id) REFERENCES menus_history (menu_id),
  CONSTRAINT childs_history_FK_to_meta_id_meta FOREIGN KEY (to_meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
*/

--
-- Table mime_types
--
CREATE TABLE mime_types (
  mime_id int(11) NOT NULL,
  mime_name varchar(50) NOT NULL,
  mime varchar(50) NOT NULL,
  lang_prefix varchar(3) NOT NULL default 'swe',
  PRIMARY KEY  (mime_id,lang_prefix)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table permission_sets
--

CREATE TABLE permission_sets (
  set_id int(11) NOT NULL,
  description varchar(30) NOT NULL,
  PRIMARY KEY  (set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table permissions
--

CREATE TABLE permissions (
  permission_id smallint(6) NOT NULL,
  lang_prefix varchar(3) NOT NULL default 'swe',
  description varchar(50) NOT NULL,
  PRIMARY KEY  (permission_id,lang_prefix)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table doc_permission_sets
--
CREATE TABLE doc_permission_sets (
  meta_id int(11) NOT NULL,
  set_id int(11) NOT NULL,
  permission_id int(11) NOT NULL,
  PRIMARY KEY  (meta_id,set_id),
  KEY doc_permission_sets_FK_set_id_permission_sets (set_id),
  CONSTRAINT doc_permission_sets_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id),
  CONSTRAINT doc_permission_sets_FK_set_id_permission_sets FOREIGN KEY (set_id) REFERENCES permission_sets (set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table new_doc_permission_sets
--
CREATE TABLE new_doc_permission_sets (
  meta_id int(11) NOT NULL,
  set_id int(11) NOT NULL,
  permission_id int(11) NOT NULL,
  PRIMARY KEY  (meta_id,set_id),
  KEY ndps_ps (set_id),
  CONSTRAINT ndps_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id),
  CONSTRAINT ndps_ps FOREIGN KEY (set_id) REFERENCES permission_sets (set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table new_doc_permission_sets_ex
--
CREATE TABLE new_doc_permission_sets_ex (
  meta_id int(11) NOT NULL,
  set_id int(11) NOT NULL,
  permission_id int(11) NOT NULL,
  permission_data int(11) NOT NULL,
  PRIMARY KEY  (meta_id,set_id,permission_id,permission_data),
  KEY ndpse_ps (set_id),
  CONSTRAINT ndpse_ndps FOREIGN KEY (meta_id, set_id) REFERENCES new_doc_permission_sets (meta_id, set_id),
  CONSTRAINT ndpse_ps FOREIGN KEY (set_id) REFERENCES permission_sets (set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table doc_permission_sets_ex
--
CREATE TABLE doc_permission_sets_ex (
  meta_id int(11) NOT NULL,
  set_id int(11) NOT NULL,
  permission_id int(11) NOT NULL,
  permission_data int(11) NOT NULL,
  PRIMARY KEY  (meta_id,set_id,permission_id,permission_data),
  KEY doc_permission_sets_ex_FK_set_id_permission_sets (set_id),
  CONSTRAINT doc_permission_sets_ex_FK_meta_id_set_id_doc_permission_sets FOREIGN KEY (meta_id, set_id) REFERENCES doc_permission_sets (meta_id, set_id),
  CONSTRAINT doc_permission_sets_ex_FK_set_id_permission_sets FOREIGN KEY (set_id) REFERENCES permission_sets (set_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table phones
--

CREATE TABLE phones (
  phone_id int(11) NOT NULL auto_increment,
  number varchar(25) NOT NULL,
  user_id int(11) NOT NULL,
  phonetype_id int(11) NOT NULL default '0',
  PRIMARY KEY  (phone_id,user_id),
  KEY phones_FK_user_id_users (user_id),
  CONSTRAINT phones_FK_user_id_users FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table phonetypes
--

CREATE TABLE phonetypes (
  phonetype_id int(11) NOT NULL,
  typename varchar(12) NOT NULL,
  lang_id int(11) NOT NULL,
  PRIMARY KEY  (phonetype_id,lang_id),
  KEY phonetypes_FK_lang_id_lang_prefixes (lang_id),
  CONSTRAINT phonetypes_FK_lang_id_lang_prefixes FOREIGN KEY (lang_id) REFERENCES lang_prefixes (lang_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table profiles
--

CREATE TABLE profiles (
  profile_id int(11) NOT NULL auto_increment,
  name varchar(255) NOT NULL,
  document_name varchar(255) NOT NULL,
  PRIMARY KEY  (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table roles
--

CREATE TABLE roles (
  role_id int(11) NOT NULL auto_increment,
  role_name varchar(60) NOT NULL,
  permissions int(11) NOT NULL default '0',
  admin_role int(11) NOT NULL default '0',
  PRIMARY KEY  (role_id),
  UNIQUE KEY UQ_roles__role_name (role_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table roles_rights
--
CREATE TABLE roles_rights (
  role_id int(11) NOT NULL,
  meta_id int(11) NOT NULL,
  set_id smallint(6) NOT NULL,
  PRIMARY KEY  (role_id,meta_id),
  KEY roles_rights_FK_meta_id_meta (meta_id),
  CONSTRAINT roles_rights_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id),
  CONSTRAINT roles_rights_FK_role_id_roles FOREIGN KEY (role_id) REFERENCES roles (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table sections
--

CREATE TABLE sections (
  section_id int(11) NOT NULL auto_increment,
  section_name varchar(50) NOT NULL,
  PRIMARY KEY  (section_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table meta_section
--
CREATE TABLE meta_section (
  meta_id int(11) NOT NULL,
  section_id int(11) NOT NULL,
  PRIMARY KEY  (meta_id,section_id),
  KEY meta_section_FK_section_id_sections (section_id),
  CONSTRAINT meta_section_FK_section_id_sections FOREIGN KEY (section_id) REFERENCES sections (section_id),
  CONSTRAINT meta_section_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table stats
--

CREATE TABLE stats (
  name varchar(120) NOT NULL,
  num int(11) NOT NULL,
  PRIMARY KEY  (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table sys_types
--

CREATE TABLE sys_types (
  type_id smallint(6) NOT NULL,
  name varchar(50) default NULL,
  PRIMARY KEY  (type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table sys_data
--

CREATE TABLE sys_data (
  sys_id smallint(6) NOT NULL auto_increment,
  type_id smallint(6) NOT NULL,
  value varchar(1000) default NULL,
  PRIMARY KEY  (sys_id,type_id),
  KEY sys_data_FK_type_id_sys_types (type_id),
  CONSTRAINT sys_data_FK_type_id_sys_types FOREIGN KEY (type_id) REFERENCES sys_types (type_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table templategroups
--

CREATE TABLE templategroups (
  group_id int(11) NOT NULL auto_increment,
  group_name varchar(50) NOT NULL,
  PRIMARY KEY  (group_id),
  UNIQUE KEY UQ_templategroups__group_name (group_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table templates_cref
--

CREATE TABLE templates_cref (
  group_id int(11) NOT NULL,
  template_name varchar(255) NOT NULL,
  PRIMARY KEY  (group_id,template_name),
  CONSTRAINT templates_cref_FK_group_id_templategroups FOREIGN KEY (group_id) REFERENCES templategroups (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table text_docs
--
CREATE TABLE text_docs (
  meta_id int(11) NOT NULL,
  template_name varchar(255) NOT NULL,
  group_id int(11) NOT NULL default '1',
  default_template_1 varchar(255) default NULL,
  default_template_2 varchar(255) default NULL,
  default_template varchar(255) default NULL,
  PRIMARY KEY  (meta_id),
  CONSTRAINT text_docs_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table texts
--
CREATE TABLE texts (
  meta_id int(11) default NULL,
  name int(11) NOT NULL,
  text longtext NOT NULL,
  type int(11) default NULL,
  counter int(11) NOT NULL auto_increment,
  language_id smallint(6) NOT NULL default '1',
  PRIMARY KEY  (counter),
  FOREIGN KEY fk__texts__i18n_languages (language_id) REFERENCES i18n_languages (language_id),
  FOREIGN KEY fk__texts__meta (meta_id) REFERENCES meta (meta_id) ON DELETE CASCADE,
  UNIQUE INDEX ux__texts__meta_id__name__language_id (meta_id, name, language_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table texts_history
--
CREATE TABLE texts_history (
  meta_id int(11) default NULL,
  name int(11) NOT NULL,
  text longtext NOT NULL,
  type int(11) default NULL,
  modified_datetime datetime NOT NULL,
  user_id int(11) default NULL,
  counter int(11) NOT NULL auto_increment,
  language_id smallint(6) NOT NULL,
  PRIMARY KEY (counter),

  FOREIGN KEY fk__texts_history__i18n_languages (language_id) REFERENCES i18n_languages (language_id),
  FOREIGN KEY fk__texts_history__meta (meta_id) REFERENCES meta (meta_id) ON DELETE CASCADE,
  FOREIGN KEY fk__texts_history__users (user_id) REFERENCES users (user_id) ON DELETE SET NULL

) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table url_docs
--
CREATE TABLE url_docs (
  meta_id int(11) NOT NULL,
  frame_name varchar(80) NOT NULL,
  target varchar(15) NOT NULL,
  url_ref varchar(255) NOT NULL,
  url_txt varchar(255) NOT NULL,
  lang_prefix varchar(3) NOT NULL,
  PRIMARY KEY  (meta_id,lang_prefix),
  CONSTRAINT url_docs_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Table user_rights
--
CREATE TABLE user_rights (
  user_id int(11) NOT NULL,
  meta_id int(11) NOT NULL,
  permission_id smallint(6) NOT NULL,
  PRIMARY KEY  (user_id,meta_id,permission_id),
  KEY user_rights_FK_meta_id_meta (meta_id),
  CONSTRAINT user_rights_FK_meta_id_meta FOREIGN KEY (meta_id) REFERENCES meta (meta_id),
  CONSTRAINT user_rights_FK_user_id_users FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table user_roles_crossref
--

CREATE TABLE user_roles_crossref (
  user_id int(11) NOT NULL,
  role_id int(11) NOT NULL,
  PRIMARY KEY  (user_id,role_id),
  KEY user_roles_crossref_FK_role_id_roles (role_id),
  CONSTRAINT user_roles_crossref_FK_role_id_roles FOREIGN KEY (role_id) REFERENCES roles (role_id),
  CONSTRAINT user_roles_crossref_FK_user_id_users FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table useradmin_role_crossref
--

CREATE TABLE useradmin_role_crossref (
  user_id int(11) NOT NULL,
  role_id int(11) NOT NULL,
  PRIMARY KEY  (user_id,role_id),
  KEY useradmin_role_crossref_FK_role_id_roles (role_id),
  CONSTRAINT useradmin_role_crossref_FK_user_id_users FOREIGN KEY (user_id) REFERENCES users (user_id),
  CONSTRAINT useradmin_role_crossref_FK_role_id_roles FOREIGN KEY (role_id) REFERENCES roles (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ### META_ID
--
-- Tables for content loop data
--
CREATE TABLE contents (
  content_id int auto_increment PRIMARY KEY,
  content_no int NOT NULL,
  meta_id int NULL,
  base_index int NOT NULL,

  UNIQUE INDEX ux__meta_id__content_no (meta_id, content_no),
  FOREIGN KEY fk__contents__meta (meta_id) REFERENCES meta (meta_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE content_loops (
  loop_id int auto_increment PRIMARY KEY,
  content_id int NOT NULL,
  loop_index int NOT NULL,
  order_index int NOT NULL,

  UNIQUE INDEX ux__content_id__loop_index (content_id, loop_index),
  UNIQUE INDEX ux__content_id__order_index (content_id, order_index),
  UNIQUE INDEX ux__content_id__loop_index__order_index (content_id, loop_index, order_index),
  FOREIGN KEY fk__content_loops__contents (content_id) REFERENCES contents (content_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;