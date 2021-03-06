-- quality management
create table o_qual_report_access (
  id bigserial,
  creationdate timestamp not null,
  lastmodified timestamp not null,
  q_type varchar(64),
  q_role varchar(64),
  q_online bool default false,
  q_email_trigger varchar(64),
  fk_data_collection bigint,
  fk_generator bigint,
  fk_group bigint,
  primary key (id)
);

alter table o_qual_report_access add constraint qual_repacc_to_dc_idx foreign key (fk_data_collection) references o_qual_data_collection (id);
create index o_qual_report_access_dc_idx on o_qual_report_access(fk_data_collection);
alter table o_qual_report_access add constraint qual_repacc_to_generator_idx foreign key (fk_generator) references o_qual_generator (id);
create index o_qual_report_access_gen_idx on o_qual_report_access(fk_generator);


-- repository
alter table o_repositoryentry add column bookable boolean default false not null;
alter table o_repositoryentry alter column objectives type varchar(32000);
alter table o_repositoryentry alter column requirements type varchar(32000);
alter table o_repositoryentry alter column credits type varchar(32000);
alter table o_repositoryentry alter column expenditureofwork type varchar(32000);

-- binder
alter table o_pf_assignment add column p_template bool default false;
alter table o_pf_assignment add column fk_binder_id bigint;
alter table o_pf_assignment add constraint pf_assign_binder_idx foreign key (fk_binder_id) references o_pf_binder (id);
create index idx_pf_assign_binder_idx on o_pf_assignment (fk_binder_id);

alter table o_pf_assignment alter column fk_section_id drop not null;



