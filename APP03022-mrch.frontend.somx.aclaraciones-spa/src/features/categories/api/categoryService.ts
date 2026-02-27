// src/features/categories/api/categoryService.ts
import ConfigurationBuilder from '@/configuration/ConfigurationBuilder';
import type { Category } from './category';

const client = ConfigurationBuilder.client as any;

/** Lista de categorías (incluye iconos en base64 si existen) */
export async function getCategories(opts: {
    page?: number;
    size?: number;
    active?: string;
} = {}) {
    return client.getFaqCategories({
        includeIcons: true,
        ...opts,
    });
}

/** Crear categoría */
export async function createCategory(body: Category): Promise<Category> {
    return client.postFaqCategory(body);
}

/** Actualizar categoría */
export async function updateCategory(id: number, body: Category): Promise<Category> {
    return client.putFaqCategory(id, body);
}

/** Publicar categoría */
export async function publishCategory(id: number) {
    await client.postCategoryPublish(id);
}

/** Despublicar categoría */
export async function unpublishCategory(id: number) {
    await client.postCategoryUnpublish(id);
}

/** Eliminar categoría */
export async function deleteCategory(id: number) {
    await client.deleteCategory(id);
}

/**
 * ¿La categoría tiene FAQs asociadas?
 * (consulta FAQs con size=1 como verificación rápida)
 */
export async function hasFaqsInCategory(categoryId: number): Promise<boolean> {
    const res = await client.getFaqs({
        categoryId,
        size: 1,
        searchTerm: '_', // truco para no filtrar por texto
    });
    return Array.isArray(res) && res.length > 0;
}

/** Obtener una categoría por ID */
export async function getCategoryById(id: number): Promise<Category | null> {
    try {
        const data = await client.getFaqCategory(id, { includeIcons: true });
        return data as Category;
    } catch (err) {
        console.error('Error fetching category by id', err);
        return null;
    }
}
