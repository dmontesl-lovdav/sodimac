import { Entity, Column, PrimaryColumn } from 'typeorm';

@Entity({ name: 'activity_logs' })
export class ActivityLogs {
    // Primary key UUID generado en Node (no requiere uuid-ossp)
    @PrimaryColumn('uuid', { name: 'activity_logs_uuid' })
    activityLogsid!: string;

    @Column('uuid', { name: 'trace_id' })
    traceid!: string;

    @Column('uuid', { name: 'trace_front_id' })
    traceFrontid!: string;

    // precision = dígitos totales, scale = decimales
    @Column({ name: 'duration_ms', type: 'decimal', precision: 18, scale: 6, nullable: true })
    durationMs!: string; // usa string para numeric/decimal

    @Column({ name: 'is_error', type: 'boolean'})
    isError!: boolean;

    @Column({ name: 'modulo', type: 'varchar', nullable: true })
    modulo?: string | null;

    @Column({ name: 'service_name', type: 'varchar'})
    serviceName?: string | null;

    @Column({ name: 'action', type: 'varchar' })
    action?: string | null;

    @Column({ name: 'message', type: 'varchar' })
    message?: string | null;

    @Column({ name: 'message_detail', type: 'text', nullable: true })
    messageDetail?: string | null;

    @Column({ name: 'user_id', type: 'varchar', nullable: true })
    userId?: string | null;

    @Column({ name: 'timestamp', type: 'timestamp' })
    timestamp?: Date | null;

    @Column({ name: 'details', type: 'simple-json', nullable: true })
    details?: string | null;

}
