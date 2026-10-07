import { useQuery } from '@tanstack/react-query';

import api from '@/utils/api';
import type { ApiRes, UserRequests } from '@/types';

function useGetPendingRequests() {
    return useQuery({
        queryKey: ['getPendingRequests'],
        queryFn: async () => {
            const { data }: { data: ApiRes<UserRequests> } = await api.get('/equipment/request/get-pending');

            return data;
        },
        retry: false,
        staleTime: 10 * 60 * 1000
    });
}

export default useGetPendingRequests;