import { useQuery } from '@tanstack/react-query';

import api from '@/utils/api';

import type { ApiRes, UserAsset } from '@/types';

function useGetUserAsset() {
    return useQuery({
        queryKey: ['getUserAsset'],
        queryFn: async () => {
            const { data }: { data: ApiRes<UserAsset> } = await api.get('/asset/get-assigned');

            return data;
        },
        retry: false,
        staleTime: 10 * 60 * 1000
    });
}

export default useGetUserAsset;