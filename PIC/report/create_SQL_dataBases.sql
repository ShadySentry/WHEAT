/* To prevent any potential data loss issues, you should review this script in detail before running it outside the context of the database designer.*/
use iFix
go

BEGIN TRANSACTION
SET QUOTED_IDENTIFIER ON
SET ARITHABORT ON
SET NUMERIC_ROUNDABORT OFF
SET CONCAT_NULL_YIELDS_NULL ON
SET ANSI_NULLS ON
SET ANSI_PADDING ON
SET ANSI_WARNINGS ON
COMMIT
BEGIN TRANSACTION
GO

if OBJECT_ID('dbo.TIME_RANGES_FILTER_SETTINGS', 'U') is not null
drop table dbo.TIME_RANGES_FILTER_SETTINGS
go

if OBJECT_ID('dbo.[TIME_SERIES_SETTINGS]', 'U') is not null
drop table dbo.[TIME_SERIES_SETTINGS]
go

CREATE TABLE [dbo].[TIME_SERIES_SETTINGS](
	[row_number] [int] NOT NULL,
	[start_date_time] [datetime] NULL,
	[end_date_time] [datetime] NULL,
	[total_rows] [int] NULL,
 CONSTRAINT [PK_TIME_SERIES_SETTINGS] PRIMARY KEY CLUSTERED 
(
	[row_number] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

ALTER TABLE [dbo].[TIME_SERIES_SETTINGS] ADD  CONSTRAINT [DF_TIME_SERIES_SETTINGS_total_rows]  DEFAULT ((5)) FOR [total_rows]
GO



/* TAG name settings table */
if OBJECT_ID('dbo.REPORT_TAG_NAME_FILTER', 'U') is not null
	drop table dbo.REPORT_TAG_NAME_FILTER
go

CREATE TABLE dbo.REPORT_TAG_NAME_FILTER
	(
	tag_name char(256) NULL,
	row_number int NOT NULL,
	total_rows int NULL
	)  ON [PRIMARY]
GO
DECLARE @v sql_variant 
SET @v = N'tag_name from Historian DB'
EXECUTE sp_addextendedproperty N'MS_Description', @v, N'SCHEMA', N'dbo', N'TABLE', N'REPORT_TAG_NAME_FILTER', N'COLUMN', N'tag_name'
GO
DECLARE @v sql_variant 
SET @v = N'record ID'
EXECUTE sp_addextendedproperty N'MS_Description', @v, N'SCHEMA', N'dbo', N'TABLE', N'REPORT_TAG_NAME_FILTER', N'COLUMN', N'row_number'
GO
ALTER TABLE dbo.REPORT_TAG_NAME_FILTER ADD CONSTRAINT
	DF_REPORT_TAG_NAME_FILTER_total_rows DEFAULT 5 FOR total_rows
GO
ALTER TABLE dbo.REPORT_TAG_NAME_FILTER SET (LOCK_ESCALATION = TABLE)
GO

/* pupulate [REPORT_TAG_NAME_FILTER]*/
insert into [iFix].[dbo].[REPORT_TAG_NAME_FILTER]
	values('Hist.FIX.HMI_PH_DD01.F_CV',1,5);
go
insert into [iFix].[dbo].[REPORT_TAG_NAME_FILTER]
	values('Hist.FIX.HMI_PH_DD02.F_CV',2,5);
go
insert into [iFix].[dbo].[REPORT_TAG_NAME_FILTER]
	values('Hist.FIX.HMI_PH_DT03.F_CV',3,5);
go
insert into [iFix].[dbo].[REPORT_TAG_NAME_FILTER]
	values('Hist.FIX.HMI_PH_DT04.F_CV',4,5);
go
insert into [iFix].[dbo].[REPORT_TAG_NAME_FILTER]
	values('Hist.FIX.HMI_PH_DD05.F_CV',5,5);
go

/*populate [TIME_SERIES_SETTINGS]*/

insert into [iFix].[dbo].[TIME_SERIES_SETTINGS]
	values(1,'2022-03-20 14:23:00','2022-03-20 14:37:00',5);
go
insert into [iFix].[dbo].[TIME_SERIES_SETTINGS]
	values(2,'2022-03-20 14:40:00',' 2022-03-20 16:55:00',5);
go
insert into [iFix].[dbo].[TIME_SERIES_SETTINGS]
	values(3,'2022-03-21 10:33:00','2022-03-21 14:33:21',5);
go
insert into [iFix].[dbo].[TIME_SERIES_SETTINGS]
	values(4,'2022-03-21 14:50:00','2022-03-21 17:25:00',5);
go
insert into [iFix].[dbo].[TIME_SERIES_SETTINGS]
	values(5,'2022-03-29 11:45:00','2022-03-29 13:35:00',5);
go
COMMIT
