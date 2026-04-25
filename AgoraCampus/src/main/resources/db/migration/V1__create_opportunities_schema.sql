CREATE TABLE app_users (
    app_user_id BIGSERIAL PRIMARY KEY,
    keycloak_id VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE profiles (
    profile_id BIGSERIAL PRIMARY KEY,
    app_user_id BIGINT NOT NULL UNIQUE REFERENCES app_users (app_user_id),
    header VARCHAR(255),
    description TEXT,
    location VARCHAR(255),
    website VARCHAR(512),
    profile_picture VARCHAR(512),
    cover_image VARCHAR(512),
    profile_type VARCHAR(32) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_profiles_profile_type
        CHECK (profile_type IN ('ORGANIZATION', 'INDIVIDUAL'))
);

CREATE TABLE organization_profiles (
    organization_profile_id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL UNIQUE REFERENCES profiles (profile_id) ON DELETE CASCADE,
    organization_name VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    industry VARCHAR(255),
    specialties VARCHAR(255)
);

CREATE TABLE individual_profiles (
    individual_profile_id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL UNIQUE REFERENCES profiles (profile_id) ON DELETE CASCADE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(50),
    cv_document VARCHAR(512),
    education VARCHAR(255),
    education_period VARCHAR(100),
    work_experience VARCHAR(255),
    other_projects VARCHAR(255)
);

CREATE TABLE opportunities (
    opportunity_id BIGSERIAL PRIMARY KEY,
    posted_by_user_id BIGINT NOT NULL REFERENCES app_users (app_user_id),
    organization_profile_id BIGINT REFERENCES organization_profiles (organization_profile_id),
    individual_profile_id BIGINT REFERENCES individual_profiles (individual_profile_id),
    title VARCHAR(255) NOT NULL,
    type VARCHAR(32) NOT NULL,
    location VARCHAR(255) NOT NULL,
    period VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    additional_info VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_opportunities_type
        CHECK (type IN ('VOLUNTEERING')),
    CONSTRAINT chk_opportunities_single_profile
        CHECK (num_nonnulls(organization_profile_id, individual_profile_id) = 1)
);

CREATE TABLE volunteering (
    volunteering_id BIGSERIAL PRIMARY KEY,
    opportunity_id BIGINT NOT NULL UNIQUE REFERENCES opportunities (opportunity_id) ON DELETE CASCADE,
    cause VARCHAR(255) NOT NULL,
    schedule VARCHAR(255),
    benefits VARCHAR(255)
);

CREATE TABLE opportunity_applications (
    application_id BIGSERIAL PRIMARY KEY,
    opportunity_id BIGINT NOT NULL REFERENCES opportunities (opportunity_id) ON DELETE CASCADE,
    applicant_user_id BIGINT NOT NULL REFERENCES app_users (app_user_id),
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    applied_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_opportunity_applications_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN')),
    CONSTRAINT uk_opportunity_applicant UNIQUE (opportunity_id, applicant_user_id)
);

CREATE INDEX idx_opportunities_type_created_at
    ON opportunities (type, created_at DESC);

CREATE INDEX idx_opportunities_location
    ON opportunities (location);

CREATE INDEX idx_opportunities_posted_by_user_id
    ON opportunities (posted_by_user_id);

CREATE INDEX idx_opportunity_applications_opportunity_id
    ON opportunity_applications (opportunity_id);

CREATE INDEX idx_opportunity_applications_applicant_user_id
    ON opportunity_applications (applicant_user_id);
