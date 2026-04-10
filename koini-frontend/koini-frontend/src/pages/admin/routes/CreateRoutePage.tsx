import React from 'react';
import { CreateRouteForm } from '../../../components/forms/CreateRouteForm';

export default function CreateRoutePage(): JSX.Element {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-text-primary">Create Route</h1>
      <CreateRouteForm />
    </div>
  );
}
