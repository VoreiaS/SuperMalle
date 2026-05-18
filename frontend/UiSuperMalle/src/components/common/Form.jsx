import React from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

const Form = ({
  schema,
  defaultValues = {},
  onSubmit,
  children,
  submitText = 'Submit',
  loading = false,
  submitButtonClass = '',
  submitButtonProps = {},
  ...props
}) => {
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
    watch,
    setValue,
    getValues,
    trigger,
    clearErrors
  } = useForm({
    resolver: zodResolver(schema),
    defaultValues,
    mode: 'onBlur'
  });

  const handleFormSubmit = async (data) => {
    try {
      await onSubmit(data, { reset, setValue, getValues });
    } catch (error) {
      console.error('Form submission error:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} {...props}>
      {typeof children === 'function'
        ? children({
            register,
            errors,
            isSubmitting,
            watch,
            setValue,
            getValues,
            trigger,
            clearErrors
          })
        : React.Children.map(children, (child) => {
            if (React.isValidElement(child)) {
              return React.cloneElement(child, {
                register,
                errors,
                isSubmitting,
                watch,
                setValue,
                getValues,
                trigger,
                clearErrors
              });
            }
            return child;
          })}
    </form>
  );
};

const FormInput = ({
  label,
  name,
  type = 'text',
  placeholder = '',
  required = false,
  disabled = false,
  register,
  errors,
  className = '',
  ...props
}) => {
  const error = errors?.[name];

  return (
    <div className={`mb-4 ${className}`}>
      {label && (
        <label
          htmlFor={name}
          className="block text-sm font-medium text-text-secondary mb-1"
        >
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <input
        id={name}
        type={type}
        placeholder={placeholder}
        disabled={disabled}
        {...register(name)}
        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 ${
          error
            ? 'border-red-500 focus:ring-red-500'
            : 'border-border-subtle'
        } ${disabled ? 'bg-bg-hover cursor-not-allowed' : 'bg-bg-surface text-text-primary'}`}
        {...props}
      />
      {error && (
        <p className="mt-1 text-sm text-red-500">{error.message}</p>
      )}
    </div>
  );
};

const FormTextarea = ({
  label,
  name,
  placeholder = '',
  rows = 4,
  required = false,
  disabled = false,
  register,
  errors,
  className = '',
  ...props
}) => {
  const error = errors?.[name];

  return (
    <div className={`mb-4 ${className}`}>
      {label && (
        <label
          htmlFor={name}
          className="block text-sm font-medium text-text-secondary mb-1"
        >
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <textarea
        id={name}
        placeholder={placeholder}
        rows={rows}
        disabled={disabled}
        {...register(name)}
        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 resize-none ${
          error
            ? 'border-red-500 focus:ring-red-500'
            : 'border-border-subtle'
        } ${disabled ? 'bg-bg-hover cursor-not-allowed' : 'bg-bg-surface text-text-primary'}`}
        {...props}
      />
      {error && (
        <p className="mt-1 text-sm text-red-500">{error.message}</p>
      )}
    </div>
  );
};

const FormSelect = ({
  label,
  name,
  options = [],
  placeholder = 'Select an option',
  required = false,
  disabled = false,
  register,
  errors,
  className = '',
  ...props
}) => {
  const error = errors?.[name];

  return (
    <div className={`mb-4 ${className}`}>
      {label && (
        <label
          htmlFor={name}
          className="block text-sm font-medium text-text-secondary mb-1"
        >
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <select
        id={name}
        disabled={disabled}
        {...register(name)}
        className={`w-full px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-copper-500/40 focus:border-copper-500 ${
          error
            ? 'border-red-500 focus:ring-red-500'
            : 'border-border-subtle'
        } ${disabled ? 'bg-bg-hover cursor-not-allowed' : 'bg-bg-surface text-text-primary'}`}
        {...props}
      >
        {placeholder && (
          <option value="">{placeholder}</option>
        )}
        {options.map((option) => (
          <option
            key={option.value}
            value={option.value}
            disabled={option.disabled}
          >
            {option.label}
          </option>
        ))}
      </select>
      {error && (
        <p className="mt-1 text-sm text-red-500">{error.message}</p>
      )}
    </div>
  );
};

const FormCheckbox = ({
  label,
  name,
  required = false,
  disabled = false,
  register,
  errors,
  className = '',
  ...props
}) => {
  const error = errors?.[name];

  return (
    <div className={`mb-4 ${className}`}>
      <div className="flex items-center">
        <input
          id={name}
          type="checkbox"
          disabled={disabled}
          {...register(name)}
          className={`w-4 h-4 text-copper-500 border-border-subtle rounded focus:ring-copper-500/40 ${
            error ? 'border-red-500' : ''
          } ${disabled ? 'bg-bg-hover cursor-not-allowed' : 'bg-bg-surface'}`}
          {...props}
        />
        {label && (
          <label
            htmlFor={name}
            className="ml-2 text-sm text-text-secondary"
          >
            {label}
            {required && <span className="text-red-500 ml-1">*</span>}
          </label>
        )}
      </div>
      {error && (
        <p className="mt-1 text-sm text-red-500">{error.message}</p>
      )}
    </div>
  );
};

const FormRadioGroup = ({
  label,
  name,
  options = [],
  required = false,
  disabled = false,
  register,
  errors,
  className = '',
  ...props
}) => {
  const error = errors?.[name];

  return (
    <div className={`mb-4 ${className}`}>
      {label && (
        <label className="block text-sm font-medium text-text-secondary mb-2">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      <div className="space-y-2">
        {options.map((option) => (
          <div key={option.value} className="flex items-center">
            <input
              id={`${name}-${option.value}`}
              type="radio"
              value={option.value}
              disabled={disabled}
              {...register(name)}
              className={`w-4 h-4 text-copper-500 border-border-subtle focus:ring-copper-500/40 ${
                error ? 'border-red-500' : ''
              } ${disabled ? 'bg-bg-hover cursor-not-allowed' : 'bg-bg-surface'}`}
              {...props}
            />
            <label
              htmlFor={`${name}-${option.value}`}
              className="ml-2 text-sm text-text-secondary"
            >
              {option.label}
            </label>
          </div>
        ))}
      </div>
      {error && (
        <p className="mt-1 text-sm text-red-500">{error.message}</p>
      )}
    </div>
  );
};

const FormSubmitButton = ({
  text = 'Submit',
  loading = false,
  disabled = false,
  className = '',
  ...props
}) => {
  return (
    <button
      type="submit"
      disabled={disabled || loading}
      className={`btn-copper w-full ${className}`}
      {...props}
    >
      {loading ? (
        <span className="flex items-center justify-center">
          <svg
            className="animate-spin -ml-1 mr-3 h-5 w-5 text-white"
            xmlns="http://www.w3.org/2000/svg"
            fill="none"
            viewBox="0 0 24 24"
          >
            <circle
              className="opacity-25"
              cx="12"
              cy="12"
              r="10"
              stroke="currentColor"
              strokeWidth="4"
            ></circle>
            <path
              className="opacity-75"
              fill="currentColor"
              d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
            ></path>
          </svg>
          Processing...
        </span>
      ) : (
        text
      )}
    </button>
  );
};

const FormError = ({ message }) => {
  if (!message) return null;

  return (
    <div className="mb-4 p-4 bg-red-500/10 border border-red-500/20 rounded-lg">
      <div className="flex">
        <svg
          className="w-5 h-5 text-red-500 mr-2 shrink-0"
          fill="currentColor"
          viewBox="0 0 20 20"
        >
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
            clipRule="evenodd"
          />
        </svg>
        <p className="text-sm text-red-500">{message}</p>
      </div>
    </div>
  );
};

const FormSuccess = ({ message }) => {
  if (!message) return null;

  return (
    <div className="mb-4 p-4 bg-emerald-500/10 border border-emerald-500/20 rounded-lg">
      <div className="flex">
        <svg
          className="w-5 h-5 text-emerald-500 mr-2 shrink-0"
          fill="currentColor"
          viewBox="0 0 20 20"
        >
          <path
            fillRule="evenodd"
            d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
            clipRule="evenodd"
          />
        </svg>
        <p className="text-sm text-emerald-500">{message}</p>
      </div>
    </div>
  );
};

export {
  Form,
  FormInput,
  FormTextarea,
  FormSelect,
  FormCheckbox,
  FormRadioGroup,
  FormSubmitButton,
  FormError,
  FormSuccess
};
