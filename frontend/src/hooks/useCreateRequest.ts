import { toast } from 'react-hot-toast';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import api from '@/utils/api';

import type { AxiosError } from 'axios';
import type { ApiRes } from '@/types';

type RequestData = {
    description: string
}

function useCreateRequest() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (requestData: RequestData) => {
            const { data } : { data: ApiRes<RequestData> } = await api.post('equipment/request/create', requestData);

            return data;
        },
        onMutate: () => {
            toast.loading('Creating request', { id: 'equipmentRequest-toast' });
        },
        onSuccess: (data: ApiRes<RequestData>) => {
            toast.success(data.message || 'Request created', { id: 'equipmentRequest-toast' });

            queryClient.invalidateQueries({ queryKey: ['getUserRequests'] });
        },
        onError: (err: AxiosError<{ message: string }>) => {
            const errMessage = err.response?.data?.message || 'An error occured in creating the request';

            toast.error(errMessage, { id: 'equipmentRequest-toast' });
        }
    });
}

export default useCreateRequest;