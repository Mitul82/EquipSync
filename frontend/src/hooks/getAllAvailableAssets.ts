import { useQuery } from '@tanstack/react-query';

import api from '@/utils/api';

import type { ApiRes, Assets } from '@/types';

function useGetAllAvailableAssets() {
    return useQuery({
        queryKey: ['getAllAssets'],
        queryFn: async () => {
            const { data }: { data: ApiRes<Assets> } = await api.get('/asset/get-available');

            return data;
        },
        retry: false,
        staleTime: 10 * 60 * 1000
    });
}

export default useGetAllAvailableAssets;