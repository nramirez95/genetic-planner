
import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query';

import { planningsApi } from '../api/planningsApi';

export const planningQueryKeys = {
    all: ['plannings'] as const,
    detail: (id: string) => ['plannings', id] as const,
};

export function usePlannings() {
    return useQuery({
        queryKey: planningQueryKeys.all,
        queryFn: planningsApi.getAll,
    });
}

export function usePlanning(id: string) {
    return useQuery({
        queryKey: planningQueryKeys.detail(id),
        queryFn: () => planningsApi.getById(id),
        enabled: Boolean(id),
    });
}

export function useDeletePlanning() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: planningsApi.delete,

        onSuccess: async (_data, id) => {
            queryClient.removeQueries({
                queryKey: planningQueryKeys.detail(id),
                exact: true,
            });

            await queryClient.invalidateQueries({
                queryKey: planningQueryKeys.all,
            });
        },
    });
}
