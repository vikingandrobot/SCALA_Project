DROP SCHEMA IF EXISTS project;
CREATE SCHEMA project;

USE project;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
  id INT(11) NOT NULL AUTO_INCREMENT,
  firstName VARCHAR(255) NOT NULL,
  lastName VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  
  PRIMARY KEY (id)
);

DROP TABLE IF EXISTS organizations;
CREATE TABLE organizations (
	id INT(11) NOT NULL AUTO_INCREMENT,
    type VARCHAR(255) NOT NULL,
    name VARCHAR (255) NOT NULL,
    address VARCHAR (255) NOT NULL, 
    
	PRIMARY KEY (id)
);

DROP TABLE IF EXISTS events;
CREATE TABLE events (
	id INT(11) NOT NULL AUTO_INCREMENT,
	title VARCHAR(255) NOT NULL, 
    description VARCHAR(255) NOT NULL,
    price FLOAT NOT NULL,
    address VARCHAR(255) NOT NULL,
    region VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    schedule_description VARCHAR(255) NOT NULL,
	user_id INT(11) NOT NULL,  -- created
    organization_id INT(11) NOT NULL, -- organized by
    
    PRIMARY KEY (id),
    CONSTRAINT fx_evmt_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_evmt_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

-- Link Users have favorites events
DROP TABLE IF EXISTS favorites;
CREATE TABLE favorites (
	id INT(11) NOT NULL AUTO_INCREMENT,
    event_id INT(11) NOT NULL,
    user_id INT(11) NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT fx_fvt_event FOREIGN KEY (event_id) REFERENCES events(id),
	CONSTRAINT fx_fvt_user FOREIGN KEY (user_id) REFERENCES users(id)
);

DROP TABLE IF EXISTS comments;
CREATE TABLE comments (
	id INT(11) NOT NULL AUTO_INCREMENT,
    message VARCHAR(255) NOT NULL,
    user_id INT(11) NOT NULL,
    event_id INT(11) NOT NULL,
    
	PRIMARY KEY (id),
	CONSTRAINT fx_cmt_user FOREIGN KEY (user_id) REFERENCES users(id),
	CONSTRAINT fx_cmt_event FOREIGN KEY (event_id) REFERENCES events(id)
);

-- Link organization Managed by user
DROP TABLE IF EXISTS users_organizations;
CREATE TABLE users_organizations(
	id INT(11) NOT NULL AUTO_INCREMENT,
    user_id INT(11) NOT NULL,
    organization_id INT(11) NOT NULL,
    
    PRIMARY KEY (id),
	CONSTRAINT fx_uo_user FOREIGN KEY (user_id) REFERENCES users(id),
	CONSTRAINT fk_uo_organization FOREIGN KEY (organization_id) REFERENCES organizations(id)
);

DROP TABLE IF EXISTS notifications;
CREATE TABLE notifications(
	id INT(11) NOT NULL AUTO_INCREMENT,
    seen BOOLEAN NOT NULL,
	user_id INT(11) NOT NULL,
    event_id INT(11) NOT NULL,
	
    PRIMARY KEY (id),
    CONSTRAINT fx_ntf_user FOREIGN KEY (user_id) REFERENCES users(id),
	CONSTRAINT fx_ntf_event FOREIGN KEY (event_id) REFERENCES events(id)
);

DROP TABLE IF EXISTS themes;
CREATE TABLE themes (
	id INT(11) NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
	PRIMARY KEY (id)
);

-- Link users interested by themes
DROP TABLE IF EXISTS interest;
CREATE TABLE interests (
	id INT(11) NOT NULL AUTO_INCREMENT,
    user_id INT(11) NOT NULL,
    theme_id INT(11) NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT fx_itrs_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fx_itrs_theme FOREIGN KEY (theme_id) REFERENCES themes(id)
);

-- Link events have themes
DROP TABLE IF EXISTS themes_events;
CREATE TABLE themes_events (
	id INT(11) NOT NULL AUTO_INCREMENT,
    event_id INT(11) NOT NULL,
    theme_id INT(11) NOT NULL,
    
    PRIMARY KEY (id),
    CONSTRAINT fx_te_event FOREIGN KEY (event_id) REFERENCES events(id),
    CONSTRAINT fx_te_theme FOREIGN KEY (theme_id) REFERENCES themes(id)
);
