import { useQuery } from '@tanstack/react-query';

import api from '@/utils/api';

import type { ApiRes, Assets } from '@/types';

function useGetAllAssets() {
    return useQuery({
        queryKey: ['getAllAssets'],
        queryFn: async () => {
            const { data }: { data: ApiRes<Assets> } = await api.get('/asset/get-all');

            return data;
        },
        retry: false,
        staleTime: 10 * 60 * 1000
    });
}

export default useGetAllAssets;