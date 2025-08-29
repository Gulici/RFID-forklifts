create table firms
(
    id uuid not null,
    firm_name varchar(255) not null,
    primary key (id),
    constraint uq_firms_firm_name unique (firm_name)
);

create table locations
(
    id uuid not null,
    name varchar(255) not null,
    zone_id integer not null,
    x integer not null,
    y integer not null,
    firm_id uuid not null,
    primary key (id),

    constraint fk_locations_firm_id foreign key (firm_id) references firms
);

create table devices
(
    id uuid not null,
    name varchar(255) not null,
    fingerprint varchar(255) not null,
    firm_id uuid not null,
    location_id uuid,
    last_seen timestamp(6),
    timestamp timestamp(6),
    primary key (id),

    constraint uq_devices_fingerprint unique (fingerprint),

    constraint fk_devices_firm_id foreign key (firm_id) references firms,
    constraint fk_devices_location_id foreign key (location_id) references locations
);

create table locations_history
(
    timestamp timestamp(6) not null,
    device_id uuid not null,
    id uuid not null,
    location_id uuid not null,
    primary key (id),

    constraint fk_locations_hist_device_id foreign key (device_id) references devices,
    constraint fk_locations_hist_location_id foreign key (location_id) references locations
);

create table roles
(
    id uuid not null,
    name varchar(255) not null,
    primary key (id),

    constraint uq_roles_name unique (name),
    constraint chk_roles_name check (name in ('ROLE_ROOT', 'ROLE_ADMIN', 'ROLE_USER'))
);

create table users
(
    id uuid not null,
    firm_id uuid,
    email varchar(255) not null,
    password varchar(255) not null,
    username varchar(255) not null,
    primary key (id),

    constraint uq_users_email unique (email),
    constraint uq_users_username unique (username),

    constraint fk_users_firm_id foreign key (firm_id) references firms
);

create table user_roles
(
    role_id uuid not null,
    user_id uuid not null,
    primary key (role_id, user_id),

    constraint fk_user_roles_role_id foreign key (role_id) references roles,
    constraint fk_user_roles_user_id foreign key (user_id) references users
);
