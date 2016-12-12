CREATE TABLE IF NOT EXISTS `characters` (
	`account_name` VARCHAR(45) NOT NULL DEFAULT '',
	`obj_Id` INT NOT NULL DEFAULT '0',
	`char_name` VARCHAR(35) CHARACTER SET UTF8 NOT NULL DEFAULT '',
	`face` TINYINT UNSIGNED DEFAULT NULL,
	`hairStyle` TINYINT UNSIGNED DEFAULT NULL,
	`hairColor` TINYINT UNSIGNED DEFAULT NULL,
	`sex` BOOLEAN DEFAULT NULL,
	`heading` mediumint DEFAULT NULL,
	`x` mediumint DEFAULT NULL,
	`y` mediumint DEFAULT NULL,
	`z` mediumint DEFAULT NULL,
	`karma` INT DEFAULT NULL,
	`pvpkills` INT DEFAULT NULL,
	`pkkills` INT DEFAULT NULL,
	`clanid` INT DEFAULT NULL,
	`createtime` INT UNSIGNED NOT NULL DEFAULT '0',
	`deletetime` INT UNSIGNED NOT NULL DEFAULT '0',
	`title` VARCHAR(24) CHARACTER SET UTF8 DEFAULT NULL,
	`rec_have` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`rec_left` TINYINT UNSIGNED NOT NULL DEFAULT '20',
	`rec_bonus_time` INT NOT NULL DEFAULT '3600',
	`hunt_points` INT NOT NULL DEFAULT '0',
	`hunt_time` INT NOT NULL DEFAULT '14400',
	`accesslevel` TINYINT DEFAULT NULL,
	`online` BOOLEAN DEFAULT NULL,
	`onlinetime` INT UNSIGNED NOT NULL DEFAULT '0',
	`lastAccess` INT UNSIGNED NOT NULL DEFAULT '0',
	`leaveclan`  INT UNSIGNED NOT NULL DEFAULT '0',
	`deleteclan` INT UNSIGNED NOT NULL DEFAULT '0',
	`nochannel` INT NOT NULL DEFAULT '0', -- not UNSIGNED, negative value means 'forever'
	`pledge_type` SMALLINT NOT NULL DEFAULT '-128',
	`pledge_rank` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`lvl_joined_academy` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	`apprentice` INT UNSIGNED NOT NULL DEFAULT '0',
	`key_bindings` varbinary(8192) DEFAULT NULL,
	`pcBangPoints` INT NOT NULL DEFAULT '0',
	`vitality` SMALLINT UNSIGNED NOT NULL DEFAULT '20000',
	`fame` INT NOT NULL DEFAULT '0',
	`bookmarks` TINYINT UNSIGNED NOT NULL DEFAULT '0',
	PRIMARY KEY (obj_Id),
	UNIQUE KEY `char_name` (`char_name`),
	KEY `account_name` (`account_name`),
	KEY `clanid` (`clanid`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
