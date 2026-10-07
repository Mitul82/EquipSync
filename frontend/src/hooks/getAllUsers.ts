import { useQuery } from '@tanstack/react-query';

import api from '@/utils/api';

import type { ApiRes, AuthUser } from '@/types';

function useGetAllUsers() {
    return useQuery({
        queryKey: ['getAllUsers'],
        queryFn: async () => {
            const { data }: { data: ApiRes<AuthUser[]>} = await api.get('/user/get-all');

            return data;
        },
        retry: false,
        staleTime: 10 * 60 * 1000,
    });
}

export default useGetAllUsers;