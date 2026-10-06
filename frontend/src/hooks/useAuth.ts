import { useQuery } from "@tanstack/react-query";

import api from '@/utils/api';

import type { ApiRes, AuthUser } from '@/types';

function useAuth() {
    return useQuery({
        queryKey: ['currentUser'],
        queryFn: async () => {
            const { data }: { data: ApiRes<AuthUser> } = await api.get('/user/get-current');

            return data.data;
        },
        retry: false,
        staleTime: 10 * 60 * 1000
    });
}

export default useAuth;