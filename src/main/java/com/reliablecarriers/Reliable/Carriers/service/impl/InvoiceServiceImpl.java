package com.reliablecarriers.Reliable.Carriers.service.impl;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.reliablecarriers.Reliable.Carriers.model.Booking;
import com.reliablecarriers.Reliable.Carriers.model.Payment;
import com.reliablecarriers.Reliable.Carriers.model.Shipment;
import com.reliablecarriers.Reliable.Carriers.service.InvoiceService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    @Override
    public byte[] generateInvoicePDF(Payment payment) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        try {
            PdfFont font = PdfFontFactory.createFont(StandardFonts.HELVETICA);
            PdfFont boldFont = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);

            // Header
            Paragraph header = new Paragraph("RELIABLE CARRIERS")
                .setFont(boldFont)
                .setFontSize(20)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
            document.add(header);

            Paragraph subheader = new Paragraph("INVOICE")
                .setFont(boldFont)
                .setFontSize(16)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
            document.add(subheader);

            // Invoice details
            String invoiceNumber = generateInvoiceNumber(payment.getId());
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String invoiceDate = dateFormat.format(payment.getPaymentDate() != null ? payment.getPaymentDate() : payment.getCreatedAt());

            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            infoTable.addCell(createCell("Invoice Number:", boldFont, true));
            infoTable.addCell(createCell(invoiceNumber, font, false));
            infoTable.addCell(createCell("Invoice Date:", boldFont, true));
            infoTable.addCell(createCell(invoiceDate, font, false));
            infoTable.addCell(createCell("Transaction ID:", boldFont, true));
            infoTable.addCell(createCell(payment.getTransactionId(), font, false));
            if (payment.getReference() != null) {
                infoTable.addCell(createCell("Reference:", boldFont, true));
                infoTable.addCell(createCell(payment.getReference(), font, false));
            }
            document.add(infoTable);

            // Customer info
            if (payment.getUser() != null) {
                document.add(new Paragraph("\nBill To:").setFont(boldFont).setFontSize(12));
                document.add(new Paragraph(payment.getUser().getFirstName() + " " + payment.getUser().getLastName()).setFont(font));
                document.add(new Paragraph(payment.getUser().getEmail()).setFont(font));
                if (payment.getUser().getPhone() != null) {
                    document.add(new Paragraph(payment.getUser().getPhone()).setFont(font));
                }
            }

            // Package/Service Details
            document.add(new Paragraph("\nService Details:").setFont(boldFont).setFontSize(12).setMarginTop(10));
            
            if (payment.getShipment() != null) {
                Shipment shipment = payment.getShipment();
                Table shipmentTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
                shipmentTable.addCell(createCell("Tracking Number:", boldFont, true));
                shipmentTable.addCell(createCell(shipment.getTrackingNumber(), font, false));
                shipmentTable.addCell(createCell("Service Type:", boldFont, true));
                shipmentTable.addCell(createCell(shipment.getServiceType() != null ? shipment.getServiceType().name() : "N/A", font, false));
                shipmentTable.addCell(createCell("Status:", boldFont, true));
                shipmentTable.addCell(createCell(shipment.getStatus() != null ? shipment.getStatus().name() : "N/A", font, false));
                
                if (shipment.getWeight() != null) {
                    shipmentTable.addCell(createCell("Weight:", boldFont, true));
                    shipmentTable.addCell(createCell(shipment.getWeight() + " kg", font, false));
                }
                if (shipment.getDimensions() != null) {
                    shipmentTable.addCell(createCell("Dimensions:", boldFont, true));
                    shipmentTable.addCell(createCell(shipment.getDimensions(), font, false));
                }
                if (shipment.getDescription() != null) {
                    shipmentTable.addCell(createCell("Description:", boldFont, true));
                    shipmentTable.addCell(createCell(shipment.getDescription(), font, false));
                }
                
                document.add(shipmentTable);
                
                document.add(new Paragraph("\nPickup Address:").setFont(boldFont).setFontSize(11).setMarginTop(5));
                document.add(new Paragraph(shipment.getPickupAddress()).setFont(font));
                document.add(new Paragraph(shipment.getPickupCity() + ", " + shipment.getPickupState() + " " + shipment.getPickupZipCode()).setFont(font));
                if (shipment.getPickupCountry() != null) {
                    document.add(new Paragraph(shipment.getPickupCountry()).setFont(font));
                }
                
                document.add(new Paragraph("\nDelivery Address:").setFont(boldFont).setFontSize(11).setMarginTop(5));
                document.add(new Paragraph(shipment.getDeliveryAddress()).setFont(font));
                document.add(new Paragraph(shipment.getDeliveryCity() + ", " + shipment.getDeliveryState() + " " + shipment.getDeliveryZipCode()).setFont(font));
                if (shipment.getDeliveryCountry() != null) {
                    document.add(new Paragraph(shipment.getDeliveryCountry()).setFont(font));
                }
                
                if (shipment.getEstimatedDeliveryDate() != null) {
                    document.add(new Paragraph("\nEstimated Delivery: " + 
                        new SimpleDateFormat("yyyy-MM-dd").format(shipment.getEstimatedDeliveryDate())).setFont(font));
                }
            }
            
            if (payment.getBooking() != null) {
                Booking booking = payment.getBooking();
                Table bookingTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
                bookingTable.addCell(createCell("Booking Number:", boldFont, true));
                bookingTable.addCell(createCell(booking.getBookingNumber(), font, false));
                bookingTable.addCell(createCell("Service Type:", boldFont, true));
                bookingTable.addCell(createCell(booking.getServiceType() != null ? booking.getServiceType().name() : "N/A", font, false));
                bookingTable.addCell(createCell("Status:", boldFont, true));
                bookingTable.addCell(createCell(booking.getStatus() != null ? booking.getStatus().name() : "N/A", font, false));
                
                if (booking.getTrackingNumber() != null) {
                    bookingTable.addCell(createCell("Tracking Number:", boldFont, true));
                    bookingTable.addCell(createCell(booking.getTrackingNumber(), font, false));
                }
                if (booking.getWeight() != null) {
                    bookingTable.addCell(createCell("Weight:", boldFont, true));
                    bookingTable.addCell(createCell(booking.getWeight() + " kg", font, false));
                }
                if (booking.getDimensions() != null) {
                    bookingTable.addCell(createCell("Dimensions:", boldFont, true));
                    bookingTable.addCell(createCell(booking.getDimensions(), font, false));
                }
                if (booking.getDescription() != null) {
                    bookingTable.addCell(createCell("Description:", boldFont, true));
                    bookingTable.addCell(createCell(booking.getDescription(), font, false));
                }
                
                document.add(bookingTable);
                
                document.add(new Paragraph("\nPickup Address:").setFont(boldFont).setFontSize(11).setMarginTop(5));
                document.add(new Paragraph(booking.getPickupAddress()).setFont(font));
                document.add(new Paragraph(booking.getPickupCity() + ", " + booking.getPickupState() + " " + booking.getPickupPostalCode()).setFont(font));
                if (booking.getPickupContactName() != null) {
                    document.add(new Paragraph("Contact: " + booking.getPickupContactName() + " - " + booking.getPickupContactPhone()).setFont(font));
                }
                
                document.add(new Paragraph("\nDelivery Address:").setFont(boldFont).setFontSize(11).setMarginTop(5));
                document.add(new Paragraph(booking.getDeliveryAddress()).setFont(font));
                document.add(new Paragraph(booking.getDeliveryCity() + ", " + booking.getDeliveryState() + " " + booking.getDeliveryPostalCode()).setFont(font));
                if (booking.getDeliveryContactName() != null) {
                    document.add(new Paragraph("Contact: " + booking.getDeliveryContactName() + " - " + booking.getDeliveryContactPhone()).setFont(font));
                }
                
                if (booking.getPickupDate() != null) {
                    document.add(new Paragraph("\nPickup Date: " + 
                        new SimpleDateFormat("yyyy-MM-dd").format(booking.getPickupDate())).setFont(font));
                }
                if (booking.getEstimatedDeliveryDate() != null) {
                    document.add(new Paragraph("Estimated Delivery: " + 
                        new SimpleDateFormat("yyyy-MM-dd").format(booking.getEstimatedDeliveryDate())).setFont(font));
                }
            }

            // Payment details table with breakdown
            document.add(new Paragraph("\nPayment Breakdown:").setFont(boldFont).setFontSize(12).setMarginTop(10));
            Table paymentTable = new Table(UnitValue.createPercentArray(new float[]{2, 1})).useAllAvailableWidth();
            paymentTable.addCell(createCell("Description", boldFont, true));
            paymentTable.addCell(createCell("Amount (ZAR)", boldFont, true));
            
            String description = "Shipping Service";
            BigDecimal subtotal = BigDecimal.ZERO;
            
            // Add breakdown if booking exists
            if (payment.getBooking() != null) {
                Booking booking = payment.getBooking();
                if (booking.getBasePrice() != null) {
                    paymentTable.addCell(createCell("Base Price", font, false));
                    paymentTable.addCell(createCell("R " + booking.getBasePrice().toString(), font, false));
                    subtotal = subtotal.add(booking.getBasePrice());
                }
                if (booking.getServiceFee() != null && booking.getServiceFee().compareTo(BigDecimal.ZERO) > 0) {
                    paymentTable.addCell(createCell("Service Fee", font, false));
                    paymentTable.addCell(createCell("R " + booking.getServiceFee().toString(), font, false));
                    subtotal = subtotal.add(booking.getServiceFee());
                }
                if (booking.getInsuranceFee() != null && booking.getInsuranceFee().compareTo(BigDecimal.ZERO) > 0) {
                    paymentTable.addCell(createCell("Insurance Fee", font, false));
                    paymentTable.addCell(createCell("R " + booking.getInsuranceFee().toString(), font, false));
                    subtotal = subtotal.add(booking.getInsuranceFee());
                }
                if (booking.getFuelSurcharge() != null && booking.getFuelSurcharge().compareTo(BigDecimal.ZERO) > 0) {
                    paymentTable.addCell(createCell("Fuel Surcharge", font, false));
                    paymentTable.addCell(createCell("R " + booking.getFuelSurcharge().toString(), font, false));
                    subtotal = subtotal.add(booking.getFuelSurcharge());
                }
                description = "Booking Service - " + booking.getBookingNumber();
            } else if (payment.getShipment() != null) {
                description = "Package Shipping - " + payment.getShipment().getTrackingNumber();
                if (payment.getShipment().getShippingCost() != null) {
                    paymentTable.addCell(createCell("Shipping Cost", font, false));
                    paymentTable.addCell(createCell("R " + payment.getShipment().getShippingCost().toString(), font, false));
                    subtotal = subtotal.add(payment.getShipment().getShippingCost());
                }
            }
            
            // If no breakdown available, show single line
            if (subtotal.compareTo(BigDecimal.ZERO) == 0) {
                paymentTable.addCell(createCell(description, font, false));
                paymentTable.addCell(createCell("R " + payment.getAmount().toString(), font, false));
            }
            
            // Total
            paymentTable.addCell(createCell("Total Amount", boldFont, true));
            paymentTable.addCell(createCell("R " + payment.getAmount().toString(), boldFont, true));
            
            document.add(paymentTable);

            // Payment method and status
            document.add(new Paragraph("\nPayment Method: " + (payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : "N/A")).setFont(font));
            document.add(new Paragraph("Payment Status: " + (payment.getStatus() != null ? payment.getStatus().name() : "N/A")).setFont(font));

            // Footer
            document.add(new Paragraph("\n\nThank you for your business!")
                .setFont(font)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(20));

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            document.close();
            throw e;
        }
    }

    @Override
    public String generateInvoiceNumber(Long paymentId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        String yearMonth = dateFormat.format(new Date());
        return "INV-" + yearMonth + "-" + String.format("%06d", paymentId);
    }

    @Override
    public byte[] generateReceiptPDF(Payment payment) throws Exception {
        // Receipt is simpler - just call invoice generation
        return generateInvoicePDF(payment);
    }

    private com.itextpdf.layout.element.Cell createCell(String text, PdfFont font, boolean isHeader) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
            .add(new Paragraph(text).setFont(font).setFontSize(10));
        if (isHeader) {
            cell.setBackgroundColor(ColorConstants.LIGHT_GRAY);
            cell.setBold();
        }
        return cell;
    }
}

