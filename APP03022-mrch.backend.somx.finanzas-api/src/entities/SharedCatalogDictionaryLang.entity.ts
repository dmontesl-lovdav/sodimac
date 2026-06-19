import {
    Entity,
    PrimaryGeneratedColumn,
    Column
} from 'typeorm';

@Entity({ schema: 'shared_catalogs', name: 'dictionary_lang' })
export class SharedCatalogDictionaryLang {
    @PrimaryGeneratedColumn({ name: 'id', type: 'int' })
    id!: number;

    @Column({ name: 'dict_id', type: 'int', nullable: true })
    dictId?: number;

    @Column({ name: 'lang_id', type: 'int', nullable: true })
    langId?: number;

    @Column({ name: 'description', type: 'varchar', length: 255, nullable: true })
    description?: string;
}