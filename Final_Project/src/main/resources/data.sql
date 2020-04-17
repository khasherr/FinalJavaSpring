insert into Research_Study (research_Title, research_Area, 
research_Institution, research_Duration, posted_By, posted_Date, research_Detail, num_Participants, username) 
values ('Research A', 'Area', 'Research Institution', '0 day', 'Researcher A', '2020-04-17', 'detail', 3, 'User1');

insert into Role (rolename) values ('ROLE_USER');

insert into user_accounts (username, password, email, enabled) 
values ('User1', '$2a$10$PrI5Gk9L.tSZiW9FXhTS8O8Mz9E97k2FZbFvGFFaSsiTUIl.TCrFu', 'user1@user1.ca', true);

insert into user_accounts_roles (users_user_id, roles_id) values (1, 1);
