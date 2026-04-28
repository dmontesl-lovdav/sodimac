import {
    Column,
    CreateDateColumn,
    Entity,
    Index,
    PrimaryGeneratedColumn,
    UpdateDateColumn,
} from 'typeorm';

@Entity({ schema: 'core_utils', name: 'cat_profile' })
@Index('idx_cat_profile_status', ['status'])
export class CatProfile {
    @PrimaryGeneratedColumn({ name: 'id_profile' })
    idProfile!: number;

    @Column({ name: 'code', type: 'varchar', length: 50 })
    code!: string;

    @Column({ name: 'name', type: 'varchar', length: 100 })
    name!: string;

    @Column({ name: 'description', type: 'varchar', length: 500, nullable: true })
    description?: string;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'integer', default: 1 })
    createdBy!: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'integer', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_utils', name: 'cat_user' })
@Index('idx_cat_user_status', ['status'])
export class CatUser {
    @PrimaryGeneratedColumn({ name: 'id_user' })
    idUser!: number;

    @Column({ name: 'username', type: 'varchar', length: 100 })
    username!: string;

    @Column({ name: 'email', type: 'varchar', length: 150 })
    email!: string;

    @Column({ name: 'full_name', type: 'varchar', length: 150 })
    fullName!: string;

    @Column({ name: 'description', type: 'varchar', length: 500, nullable: true })
    description?: string;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'integer', default: 1 })
    createdBy!: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'integer', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_utils', name: 'cat_role' })
@Index('idx_cat_role_status', ['status'])
export class CatRole {
    @PrimaryGeneratedColumn({ name: 'id_role' })
    idRole!: number;

    @Column({ name: 'code', type: 'varchar', length: 50 })
    code!: string;

    @Column({ name: 'name', type: 'varchar', length: 100 })
    name!: string;

    @Column({ name: 'description', type: 'varchar', length: 500, nullable: true })
    description?: string;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'integer', default: 1 })
    createdBy!: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'integer', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_utils', name: 'cat_permission' })
@Index('idx_cat_permission_status', ['status'])
export class CatPermission {
    @PrimaryGeneratedColumn({ name: 'id_permission' })
    idPermission!: number;

    @Column({ name: 'code', type: 'varchar', length: 80 })
    code!: string;

    @Column({ name: 'name', type: 'varchar', length: 150 })
    name!: string;

    @Column({ name: 'description', type: 'varchar', length: 500, nullable: true })
    description?: string;

    @Column({ name: 'id_module', type: 'integer', nullable: true })
    idModule?: number;

    @Column({ name: 'permission_level', type: 'varchar', length: 30 })
    permissionLevel!: string;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'integer', default: 1 })
    createdBy!: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'integer', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_utils', name: 'cat_attribute_type' })
@Index('idx_cat_attribute_type_status', ['status'])
export class CatAttributeType {
    @PrimaryGeneratedColumn({ name: 'id_attribute_type' })
    idAttributeType!: number;

    @Column({ name: 'code', type: 'varchar', length: 50 })
    code!: string;

    @Column({ name: 'name', type: 'varchar', length: 100 })
    name!: string;

    @Column({ name: 'description', type: 'varchar', length: 500, nullable: true })
    description?: string;

    @Column({ name: 'allows_multiple', type: 'smallint', default: 1 })
    allowsMultiple!: number;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'integer', default: 1 })
    createdBy!: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'integer', nullable: true })
    updatedBy?: number;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}
