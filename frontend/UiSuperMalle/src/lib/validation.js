import { z } from 'zod';

// Auth Schemas
export const loginSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(6, 'Password must be at least 6 characters')
});

export const registerSchema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .min(2, 'Name must be at least 2 characters')
    .max(100, 'Name must be less than 100 characters'),
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address'),
  password: z
    .string()
    .min(1, 'Password is required')
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number')
    .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
  confirmPassword: z
    .string()
    .min(1, 'Please confirm your password')
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword']
});

export const forgotPasswordSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address')
});

export const resetPasswordSchema = z.object({
  password: z
    .string()
    .min(1, 'Password is required')
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number')
    .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
  confirmPassword: z
    .string()
    .min(1, 'Please confirm your password')
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword']
});

// Profile Schemas
export const updateProfileSchema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .min(2, 'Name must be at least 2 characters')
    .max(100, 'Name must be less than 100 characters')
    .optional(),
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address')
    .optional(),
  phone: z
    .string()
    .regex(/^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/, 'Invalid phone number')
    .optional()
    .or(z.literal('')),
  address: z
    .string()
    .min(1, 'Address is required')
    .optional()
});

export const changePasswordSchema = z.object({
  currentPassword: z
    .string()
    .min(1, 'Current password is required'),
  newPassword: z
    .string()
    .min(1, 'New password is required')
    .min(8, 'Password must be at least 8 characters')
    .regex(/[A-Z]/, 'Password must contain at least one uppercase letter')
    .regex(/[a-z]/, 'Password must contain at least one lowercase letter')
    .regex(/[0-9]/, 'Password must contain at least one number')
    .regex(/[^A-Za-z0-9]/, 'Password must contain at least one special character'),
  confirmPassword: z
    .string()
    .min(1, 'Please confirm your password')
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Passwords don't match",
  path: ['confirmPassword']
});

// Order Schemas
export const deliveryAddressSchema = z.object({
  address: z
    .string()
    .min(1, 'Address is required')
    .min(10, 'Address must be at least 10 characters')
    .max(200, 'Address must be less than 200 characters'),
  city: z
    .string()
    .min(1, 'City is required')
    .min(2, 'City must be at least 2 characters')
    .max(100, 'City must be less than 100 characters'),
  state: z
    .string()
    .min(1, 'State is required')
    .min(2, 'State must be at least 2 characters')
    .max(100, 'State must be less than 100 characters'),
  zipCode: z
    .string()
    .min(1, 'Zip code is required')
    .regex(/^[0-9]{5}(-[0-9]{4})?$/, 'Invalid zip code'),
  phone: z
    .string()
    .min(1, 'Phone number is required')
    .regex(/^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/, 'Invalid phone number'),
  deliveryInstructions: z
    .string()
    .max(500, 'Instructions must be less than 500 characters')
    .optional()
    .or(z.literal(''))
});

export const pickupOrderSchema = z.object({
  pickupTime: z
    .string()
    .min(1, 'Pickup time is required'),
  phone: z
    .string()
    .min(1, 'Phone number is required')
    .regex(/^[+]?[(]?[0-9]{1,4}[)]?[-\s.]?[(]?[0-9]{1,4}[)]?[-\s.]?[0-9]{1,9}$/, 'Invalid phone number'),
  specialInstructions: z
    .string()
    .max(500, 'Instructions must be less than 500 characters')
    .optional()
    .or(z.literal(''))
});

// Review Schemas
export const reviewSchema = z.object({
  rating: z
    .number()
    .min(1, 'Rating is required')
    .max(5, 'Rating must be between 1 and 5'),
  title: z
    .string()
    .min(1, 'Title is required')
    .min(3, 'Title must be at least 3 characters')
    .max(100, 'Title must be less than 100 characters'),
  comment: z
    .string()
    .min(1, 'Comment is required')
    .min(10, 'Comment must be at least 10 characters')
    .max(1000, 'Comment must be less than 1000 characters')
});

// Contact Schemas
export const contactSchema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .min(2, 'Name must be at least 2 characters')
    .max(100, 'Name must be less than 100 characters'),
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address'),
  subject: z
    .string()
    .min(1, 'Subject is required')
    .min(3, 'Subject must be at least 3 characters')
    .max(200, 'Subject must be less than 200 characters'),
  message: z
    .string()
    .min(1, 'Message is required')
    .min(10, 'Message must be at least 10 characters')
    .max(2000, 'Message must be less than 2000 characters')
});

// Newsletter Schema
export const newsletterSchema = z.object({
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address')
});

// Loyalty Schema
export const redeemPointsSchema = z.object({
  points: z
    .number()
    .min(1, 'Points must be greater than 0')
    .int('Points must be a whole number')
});

// Admin Schemas
export const menuItemSchema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .min(2, 'Name must be at least 2 characters')
    .max(100, 'Name must be less than 100 characters'),
  description: z
    .string()
    .min(1, 'Description is required')
    .min(10, 'Description must be at least 10 characters')
    .max(500, 'Description must be less than 500 characters'),
  price: z
    .number()
    .min(0.01, 'Price must be greater than 0')
    .max(9999.99, 'Price must be less than 10000'),
  categoryId: z
    .number()
    .min(1, 'Category is required'),
  imageUrl: z
    .string()
    .url('Invalid image URL')
    .optional()
    .or(z.literal('')),
  available: z
    .boolean()
    .default(true),
  preparationTime: z
    .number()
    .min(1, 'Preparation time must be at least 1 minute')
    .max(120, 'Preparation time must be less than 120 minutes')
    .optional()
});

export const categorySchema = z.object({
  name: z
    .string()
    .min(1, 'Name is required')
    .min(2, 'Name must be at least 2 characters')
    .max(50, 'Name must be less than 50 characters'),
  description: z
    .string()
    .max(200, 'Description must be less than 200 characters')
    .optional()
    .or(z.literal('')),
  imageUrl: z
    .string()
    .url('Invalid image URL')
    .optional()
    .or(z.literal(''))
});

export const inventorySchema = z.object({
  menuItemId: z
    .number()
    .min(1, 'Menu item is required'),
  quantity: z
    .number()
    .min(0, 'Quantity must be at least 0')
    .int('Quantity must be a whole number'),
  reorderLevel: z
    .number()
    .min(0, 'Reorder level must be at least 0')
    .int('Reorder level must be a whole number'),
  supplierName: z
    .string()
    .min(1, 'Supplier name is required')
    .max(100, 'Supplier name must be less than 100 characters'),
  supplierContact: z
    .string()
    .max(100, 'Supplier contact must be less than 100 characters')
    .optional()
    .or(z.literal(''))
});

// Export all schemas
export const schemas = {
  login: loginSchema,
  register: registerSchema,
  forgotPassword: forgotPasswordSchema,
  resetPassword: resetPasswordSchema,
  updateProfile: updateProfileSchema,
  changePassword: changePasswordSchema,
  deliveryAddress: deliveryAddressSchema,
  pickupOrder: pickupOrderSchema,
  review: reviewSchema,
  contact: contactSchema,
  newsletter: newsletterSchema,
  redeemPoints: redeemPointsSchema,
  menuItem: menuItemSchema,
  category: categorySchema,
  inventory: inventorySchema
};
