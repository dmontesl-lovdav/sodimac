SELECT r.session_id, r.status, r.command, r.wait_type, r.wait_time/1000.0 AS wait_seg,
       r.blocking_session_id, t.text AS sql_actual
FROM sys.dm_exec_requests r
CROSS APPLY sys.dm_exec_sql_text(r.sql_handle) t
WHERE r.session_id > 50
ORDER BY r.wait_time DESC;


EXEC sp_who2;