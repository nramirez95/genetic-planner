
import { apiClient } from '../../../api/client';
import type { PlanningSummary } from '../types/planning';

const BASE_PATH = '/api/plannings';

export const planningsApi = {
    async getAll(): Promise<PlanningSummary[]> {
        const response = await apiClient.get<PlanningSummary[]>(
            BASE_PATH
        );

        return response.data;
    },

    async getById(id: string): Promise<PlanningSummary> {
        const response = await apiClient.get<PlanningSummary>(
            `${BASE_PATH}/${encodeURIComponent(id)}`
        );

        return response.data;
    },

    async delete(id: string): Promise<void> {
        await apiClient.delete(
            `${BASE_PATH}/${encodeURIComponent(id)}`
        );
    },
};
