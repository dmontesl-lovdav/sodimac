import { datasource } from '@/config/typeorm-datasource.js';
import { DictionaryLang } from '@/entities/DictionaryLang.entity.js';

export const repo = () => datasource.getRepository(DictionaryLang);

export async function findById(id: number): Promise<DictionaryLang | null> {
    return repo().findOne({ where: { id } });
}

export async function findByDictIdAndLangId(dictId: number, langId: number): Promise<DictionaryLang | null> {
    return repo().findOne({ where: { dictId, langId } });
}

export async function findByLangId(langId: number): Promise<DictionaryLang[]> {
    return repo().find({ where: { langId } });
}

export async function findByDictId(dictId: number): Promise<DictionaryLang[]> {
    return repo().find({ where: { dictId } });
}

export async function save(entity: DictionaryLang): Promise<DictionaryLang> {
    return repo().save(entity);
}

