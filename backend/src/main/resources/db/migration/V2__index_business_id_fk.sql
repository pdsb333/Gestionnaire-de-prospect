-- job_offers.business_id and professionals.business_id are FK columns filtered/joined on by
-- every BusinessRepository query (findByUserIdWithJobOffers, findByUserIdWithProfessionals,
-- findByUserIdWithApplicationHistory) but had no supporting index, only the PK on businesses.id.
CREATE INDEX idx_job_offers_business_id ON job_offers (business_id);
CREATE INDEX idx_professionals_business_id ON professionals (business_id);
