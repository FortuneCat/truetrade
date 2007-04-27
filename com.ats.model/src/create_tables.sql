/*
For MySQL:
create database ats;
use ats;
grant SELECT,INSERT,UPDATE,DELETE on * to 'guest'@'localhost' identified by 'guest';

*/


drop table instrument;
create table instrument (
	id int not null AUTO_INCREMENT,
	symbol char(7) not null,
	sec_type char(4) not null,
	exchange char(6) not null,
	currency char(4) not null,
	multiplier int,
	tick_size double,
	primary key(id),
	unique(symbol, sec_type, exchange, currency)
);

drop table strat_def;
create table strat_def (
	id int not null AUTO_INCREMENT,
	data_timespan_id int not null,
	simu_timespan_id int,
	classname varchar(60) not null,
	runtime boolean not null default false,
	primary key(id)
);

drop table strat_props;
create table strat_props (
	id int not null AUTO_INCREMENT,
	strat_def_id int not null,
	param_key varchar(20) not null,
	param_value int not null,
	primary key(id),
	unique(strat_def_id, param_key)
);

drop table stdef_instr_pr;
create table stdef_instr_pr (
	instrument_id int not null,
	strat_def_id int not null,
	primary key(instrument_id, strat_def_id)
);

drop table bar_series;
create table bar_series (
	id int not null AUTO_INCREMENT,
	instrument_id int not null,
	timespan_id int not null,
	primary key(id),
	unique(instrument_id, timespan_id)
);

drop table bar;
create table bar (
	id int not null AUTO_INCREMENT,
	timespan_id int,
	open double,
	high double,
	low double,
	close double,
	volume int,
	begin_time datetime,
	end_time datetime,
	bar_series_id int,
	primary key(id)
);


insert into instrument (id, symbol, sec_type, exchange, currency) values
(1, 'AAPL', 'STK', 'SMART', 'USD'),
(2, 'GOOG', 'STK', 'SMART', 'USD');

insert into strat_def (id, data_timespan_id, simu_timespan_id, classname) values
(1, 10, 10, 'com.jsystemtrader.strategy.RandomStrategy');

insert into stdef_instr_pr (strat_def_id, instrument_id) values
(1, 1),
(1, 2);



/*
		select 
			bs.id as seriesId, 
			bs.timespan_id as timespanId,
			count(bar.id) as barCount,
			min(bar.begin_time) as beginTime,
			max(bar.end_time) as endTime
		from bar_series bs, bar bar
			where bs.id = bar.bar_series_id
		group by bs.instrument_id
		order by bs.timespan_id;

*/
