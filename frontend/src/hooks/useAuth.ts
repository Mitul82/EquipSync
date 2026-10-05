import { useQuery } from "@tanstack/react-query";

import api from '@/utils/api';

import type { ApiRes } from "@/types/ApiResponse";

function useAuth() {
    return useQuery({
        queryKey: ['currentUser'],
        queryFn: async () => {
            const { data }: { data: ApiRes } = await api.get('/user/get-current');

            return data.data;
        },
        retry: false,
        staleTime: 10 * 60 * 1000
    });
}

export default useAuth;