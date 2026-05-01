import {
    Entity,
    PrimaryGeneratedColumn,
    Column,
    Index,
    Unique
} from 'typeorm';

@Entity({ name: 'dictionary_lang', schema: 'shared_catalogs' })
@Unique('uk_dictionary_lang', ['dictId', 'langId'])
@Index('idx_dl_dict', ['dictId'])
@Index('idx_dl_lang', ['langId'])
export class DictionaryLang {
    @PrimaryGeneratedColumn({ name: 'id', type: 'integer' })
    id!: number;

    @Column({ name: 'dict_id', type: 'integer', nullable: false })
    dictId!: number;

    @Column({ name: 'lang_id', type: 'integer', nullable: false })
    langId!: number;

    @Column({ name: 'description', type: 'varchar', length: 512, nullable: false })
    description!: string;
}

