import {
    Column,
    CreateDateColumn,
    Entity,
    PrimaryColumn,
    PrimaryGeneratedColumn,
    UpdateDateColumn,
} from 'typeorm';

@Entity({ schema: 'core_security', name: 'user_data' })
export class UserData {
    @PrimaryGeneratedColumn({ name: 'user_data_id' })
    idUserData!: number;

    @Column({ name: 'sub', type: 'varchar', length: 255 })
    sub!: string;

    @Column({ name: 'preferred_username', type: 'varchar', length: 255, nullable: true })
    preferredUsername?: string | null;

    @Column({ name: 'given_name', type: 'varchar', length: 255, nullable: true })
    givenName?: string | null;

    @Column({ name: 'family_name', type: 'varchar', length: 255, nullable: true })
    familyName?: string | null;

    @Column({ name: 'email', type: 'varchar', length: 320, nullable: true })
    email?: string | null;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date | null;
}

@Entity({ schema: 'core_security', name: 'module_process' })
export class ModuleProcess {
    @PrimaryGeneratedColumn({ name: 'module_process_id' })
    idModuleProcess!: number;

    @Column({ name: 'catalog_detail_module_id', type: 'integer' })
    idCatalogDetailModule!: number;

    @Column({ name: 'catalog_detail_process_id', type: 'integer' })
    idCatalogDetailProcess!: number;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_security', name: 'profile_module' })
export class ProfileModule {
    @PrimaryColumn({ name: 'catalog_detail_profile_id', type: 'integer' })
    idCatalogDetailProfile!: number;

    @PrimaryColumn({ name: 'catalog_detail_module_id', type: 'integer' })
    idCatalogDetailModule!: number;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_security', name: 'profile_user' })
export class ProfileUser {
    @PrimaryColumn({ name: 'catalog_detail_profile_id', type: 'integer' })
    idCatalogDetailProfile!: number;

    @PrimaryColumn({ name: 'user_data_id', type: 'integer' })
    userDataId!: number;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_security', name: 'profile_module_process' })
export class ProfileModuleProcess {
    @PrimaryColumn({ name: 'catalog_detail_profile_id', type: 'integer' })
    idCatalogDetailProfile!: number;

    @PrimaryColumn({ name: 'module_process_id', type: 'integer' })
    idModuleProcess!: number;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_security', name: 'role_user' })
export class RoleUser {
    @PrimaryColumn({ name: 'catalog_detail_role_id', type: 'integer' })
    idCatalogDetailRole!: number;

    @PrimaryColumn({ name: 'user_data_id', type: 'integer' })
    userDataId!: number;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_security', name: 'role_permission' })
export class RolePermission {
    @PrimaryColumn({ name: 'catalog_detail_role_id', type: 'integer' })
    idCatalogDetailRole!: number;

    @PrimaryColumn({ name: 'catalog_detail_permission_id', type: 'integer' })
    idCatalogDetailPermission!: number;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_security', name: 'role_provider' })
export class RoleProvider {
    @PrimaryColumn({ name: 'catalog_detail_role_id', type: 'integer' })
    idCatalogDetailRole!: number;

    @PrimaryColumn({ name: 'catalog_detail_provider_id', type: 'integer' })
    idCatalogDetailProvider!: number;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_security', name: 'user_attribute' })
export class UserAttribute {
    @PrimaryGeneratedColumn({ name: 'user_attribute_id' })
    idUserAttribute!: number;

    @Column({ name: 'user_data_id', type: 'integer' })
    userDataId!: number;

    @Column({ name: 'catalog_detail_attribute_type_id', type: 'integer' })
    idCatalogDetailAttributeType!: number;

    @Column({ name: 'catalog_detail_attribute_value_id', type: 'integer', nullable: true })
    idCatalogDetailAttributeValue?: number | null;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}

@Entity({ schema: 'core_security', name: 'role_attribute' })
export class RoleAttribute {
    @PrimaryGeneratedColumn({ name: 'role_attribute_id' })
    idRoleAttribute!: number;

    @Column({ name: 'catalog_detail_role_id', type: 'integer' })
    idCatalogDetailRole!: number;

    @Column({ name: 'catalog_detail_attribute_type_id', type: 'integer' })
    idCatalogDetailAttributeType!: number;

    @Column({ name: 'catalog_detail_attribute_value_id', type: 'integer', nullable: true })
    idCatalogDetailAttributeValue?: number | null;

    @Column({ name: 'status', type: 'smallint', default: 1 })
    status!: number;

    @Column({ name: 'created_by', type: 'varchar', length: 80, default: () => "'SYSTEM'" })
    createdBy!: string;

    @CreateDateColumn({ name: 'created_at', type: 'timestamp' })
    createdAt!: Date;

    @Column({ name: 'updated_by', type: 'varchar', length: 80, nullable: true })
    updatedBy?: string;

    @UpdateDateColumn({ name: 'updated_at', type: 'timestamp', nullable: true })
    updatedAt?: Date;
}
