import { Entity, Column, PrimaryGeneratedColumn } from 'typeorm';

@Entity({ schema: 'core_audit', name: 'activity_logs' })
export class ActivityLogs {
    @PrimaryGeneratedColumn('uuid', { name: 'activity_logs_uuid' })
    activityLogsUuid!: string;

    @Column('uuid', { name: 'trace_id' })
    traceId!: string;

    @Column('uuid', { name: 'trace_front_id', nullable: true })
    traceFrontId?: string | null;

    @Column({ name: 'duration_ms', type: 'decimal', precision: 18, scale: 6, nullable: true })
    durationMs?: string | null;

    @Column({ name: 'is_error', type: 'boolean', default: false })
    isError!: boolean;

    @Column({ name: 'modulo', type: 'varchar', nullable: true })
    modulo?: string | null;

    @Column({ name: 'service_name', type: 'varchar', nullable: true })
    serviceName?: string | null;

    @Column({ name: 'action', type: 'varchar', nullable: true })
    action?: string | null;

    @Column({ name: 'paso', type: 'varchar', nullable: true })
    paso?: string | null;

    @Column({ name: 'message', type: 'varchar', nullable: true })
    message?: string | null;

    @Column({ name: 'message_detail', type: 'text', nullable: true })
    messageDetail?: string | null;

    @Column({ name: 'id_mensaje', type: 'varchar', nullable: true })
    idMensaje?: string | null;

    @Column({ name: 'codigo_error', type: 'varchar', nullable: true })
    codigoError?: string | null;

    @Column({ name: 'tipo_evento', type: 'varchar', nullable: true })
    tipoEvento?: 'ERROR' | 'ALERTA' | 'INFO' | null;

    @Column({ name: 'log', type: 'text', nullable: true })
    log?: string | null;

    @Column({ name: 'user_id', type: 'varchar', nullable: true })
    userId?: string | null;

    @Column({ name: 'timestamp', type: 'timestamp', nullable: true })
    timestamp?: Date | null;

    @Column({ name: 'details', type: 'jsonb', nullable: true })
    details?: any;
}
