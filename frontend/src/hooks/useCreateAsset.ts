import toast from 'react-hot-toast';
import { useMutation, useQueryClient } from '@tanstack/react-query';

import api from '@/utils/api';

import type { AxiosError } from 'axios';
import type { ApiRes, UserAsset } from '@/types';

type AssetData = {
    name: string,
    serialNumber: string,
    status: 'Available' | 'Assigned' | 'In_repair' | 'Retired'
}

function useCreateAsset() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (asset: AssetData) => {
            const { data }: { data: ApiRes<UserAsset> } = await api.post('/asset/create', asset);

            return data;
        },
        onMutate: () => {
            toast.loading('Creating asset...', { id: 'createAsset-toast' });
        },
        onSuccess: (data: ApiRes<UserAsset>) => {
            toast.success(data.message || 'Asset created', { id: 'createAsset-toast' });

            queryClient.invalidateQueries({ queryKey: ['getAllAssets'] });
            queryClient.invalidateQueries({ queryKey: ['getAllAvailableAssets'] });
        },
        onError: (err: AxiosError<{ message: string }>) => {
            const errMessage = err.response?.data?.message || 'An error occured in creating the asset';

            toast.error(errMessage, { id: 'create-Asset-toast' });
        }
    });
}

export default useCreateAsset;