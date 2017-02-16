package com.csulb.yelp;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.bson.Document;

import com.mongodb.Block;
import com.mongodb.client.AggregateIterable;

public class YelpMongoUI {

	private JFrame frame;
	private JList<String> mainCategoryList, attributesList;
	private JTable filteredBusinesses;
	private DefaultTableModel tModel;
	private JComboBox pointOfInterest, proximity;
	private JButton btnSearch;
	private JLabel pointOfInterestLabel;
	private JLabel labelProximity;
	private JLabel lblMainCategories;
	private JLabel lblAttributes;
	private JLabel lblMatchingBusiness;
	private JLabel toggleMainCategoryCondition;
	private JToggleButton toggleAttributeCondition;
	private JLabel lblClickToToggle;
	private JSeparator separator;

	private static final String[] mainCategories = { "Active Life", "Arts & Entertainment", "Automotive", "Car Rental", "Cafes", "Beauty & Spas",
			"Convenience Stores", "Dentists", "Doctors", "Drugstores", "Department Stores", "Education", "Event Planning & Services", "Flowers & Gifts", "Food",
			"Health & Medical", "Home Services", "Home & Garden", "Hospitals", "Hotels & Travel", "Hardware Stores", "Grocery", "Medical Centers",
			"Nurseries & Gardening", "Nightlife", "Restaurants", "Shopping", "Transportation" };

	private static final String[][] pointsOfinterests = { { "Red Lobster, ON", "-80.432996, 43.42898" },
			{ "Staybridge Suites Extended Stay Hotel, WI", "-89.516037999999995, 43.096271000000002" },
			{ "Yafo Kosher Restaurant, NV", "-115.16488579999999, 36.131443900000001" }, { "Imagine Spain, AZ", "-111.9301931, 33.5825532" },
			{ "Sauced, NV", "-115.076425261377, 36.145384575890297" } };

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					YelpMongoUI window = new YelpMongoUI();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public YelpMongoUI() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 1176, 681);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0 };
		frame.getContentPane().setLayout(gridBagLayout);

		JLabel txtpnUseCtrl = new JLabel();
		txtpnUseCtrl.setText("Use Ctrl + Click to select multiple Categories. Click on a row to see the business' reviews");
		GridBagConstraints c1 = new GridBagConstraints();
		c1.ipadx = 20;
		c1.insets = new Insets(0, 0, 5, 0);
		c1.gridwidth = 4;
		c1.fill = GridBagConstraints.BOTH;
		c1.gridx = 0;
		c1.fill = GridBagConstraints.HORIZONTAL;
		c1.gridy = 0;
		frame.getContentPane().add(txtpnUseCtrl, c1);

		// Main category section

		lblMainCategories = new JLabel("Main Categories");
		lblMainCategories.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblMainCategories = new GridBagConstraints();
		gbc_lblMainCategories.insets = new Insets(0, 0, 5, 5);
		gbc_lblMainCategories.gridx = 0;
		gbc_lblMainCategories.gridy = 1;
		frame.getContentPane().add(lblMainCategories, gbc_lblMainCategories);

		mainCategoryList = new JList<String>();
		GridBagConstraints c2 = new GridBagConstraints();
		c2.insets = new Insets(0, 0, 5, 5);
		c2.weightx = 0.2;
		c2.fill = GridBagConstraints.BOTH;
		c2.gridx = 0;
		c2.gridy = 2;
		frame.getContentPane().add(new JScrollPane(mainCategoryList), c2);
		mainCategoryList.setListData(mainCategories);

		// Attributes section

		lblAttributes = new JLabel("Attributes");
		lblAttributes.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblAttributes = new GridBagConstraints();
		gbc_lblAttributes.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttributes.gridx = 1;
		gbc_lblAttributes.gridy = 1;
		frame.getContentPane().add(lblAttributes, gbc_lblAttributes);

		attributesList = new JList<String>();
		GridBagConstraints c4 = new GridBagConstraints();
		c4.insets = new Insets(0, 0, 5, 5);
		c4.weightx = 0.2;
		c4.fill = GridBagConstraints.BOTH;
		c4.gridx = 1;
		c4.gridy = 2;
		frame.getContentPane().add(new JScrollPane(attributesList), c4);

		// Businesses

		lblMatchingBusiness = new JLabel("Matching Business");
		lblMatchingBusiness.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_lblMatchingBusiness = new GridBagConstraints();
		gbc_lblMatchingBusiness.insets = new Insets(0, 0, 5, 0);
		gbc_lblMatchingBusiness.gridwidth = 2;
		gbc_lblMatchingBusiness.gridx = 2;
		gbc_lblMatchingBusiness.gridy = 1;
		frame.getContentPane().add(lblMatchingBusiness, gbc_lblMatchingBusiness);

		tModel = new DefaultTableModel() {

			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		filteredBusinesses = new JTable();
		filteredBusinesses.setModel(tModel);
		tModel.addColumn("ID");
		tModel.addColumn("Name");
		tModel.addColumn("City");
		tModel.addColumn("State");
		tModel.addColumn("Stars");
		filteredBusinesses.getColumnModel().getColumn(0).setWidth(0);
		filteredBusinesses.getColumnModel().getColumn(0).setMinWidth(0);
		filteredBusinesses.getColumnModel().getColumn(0).setMaxWidth(0);

		GridBagConstraints c5 = new GridBagConstraints();
		c5.insets = new Insets(0, 0, 5, 0);
		c5.weightx = 0.4;
		c5.gridwidth = 2;
		c5.fill = GridBagConstraints.BOTH;
		c5.gridx = 2;
		c5.gridy = 2;
		frame.getContentPane().add(new JScrollPane(filteredBusinesses), c5);

		String[] addressOfInterest = { "-", pointsOfinterests[0][0], pointsOfinterests[1][0], pointsOfinterests[2][0], pointsOfinterests[3][0],
				pointsOfinterests[4][0] };

		lblClickToToggle = new JLabel("Click To Toggle");
		GridBagConstraints gbc_lblClickToToggle = new GridBagConstraints();
		gbc_lblClickToToggle.gridwidth = 2;
		gbc_lblClickToToggle.insets = new Insets(0, 0, 5, 5);
		gbc_lblClickToToggle.gridx = 0;
		gbc_lblClickToToggle.gridy = 3;
		frame.getContentPane().add(lblClickToToggle, gbc_lblClickToToggle);

		toggleMainCategoryCondition = new JLabel("OR");
		GridBagConstraints gbc_toggleMainCategoryCondition = new GridBagConstraints();
		gbc_toggleMainCategoryCondition.insets = new Insets(0, 0, 5, 5);
		gbc_toggleMainCategoryCondition.gridx = 0;
		gbc_toggleMainCategoryCondition.gridy = 4;
		frame.getContentPane().add(toggleMainCategoryCondition, gbc_toggleMainCategoryCondition);

		toggleAttributeCondition = new JToggleButton("OR");
		GridBagConstraints gbc_toggleAttributeCondition = new GridBagConstraints();
		gbc_toggleAttributeCondition.insets = new Insets(0, 0, 5, 5);
		gbc_toggleAttributeCondition.gridx = 1;
		gbc_toggleAttributeCondition.gridy = 4;
		frame.getContentPane().add(toggleAttributeCondition, gbc_toggleAttributeCondition);

		toggleAttributeCondition.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractButton abstractButton = (AbstractButton) e.getSource();
				if (abstractButton.getModel().isSelected()) {
					abstractButton.setText("AND");
				} else {
					abstractButton.setText("OR");
				}
				int address = pointOfInterest.getSelectedIndex() - 1;
				if (address >= 0) {
					String longLatt = pointsOfinterests[address][1];
					String longitude = longLatt.split(",")[0].trim();
					String lattitude = longLatt.split(",")[1].trim();
					int proximityMiles = Integer.parseInt((String) proximity.getSelectedItem());

					populateBusiness(longitude, lattitude, proximityMiles);
				} else {
					populateBusiness(null, null, 0);
				}
			}
		});

		separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 2;
		gbc_separator.fill = GridBagConstraints.HORIZONTAL;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 5;
		frame.getContentPane().add(separator, gbc_separator);

		pointOfInterestLabel = new JLabel("Point of Interest");
		pointOfInterestLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_pointOfInterestLabel = new GridBagConstraints();
		gbc_pointOfInterestLabel.insets = new Insets(0, 0, 5, 5);
		gbc_pointOfInterestLabel.weightx = 0.25;
		gbc_pointOfInterestLabel.gridx = 0;
		gbc_pointOfInterestLabel.gridy = 6;
		frame.getContentPane().add(pointOfInterestLabel, gbc_pointOfInterestLabel);

		pointOfInterest = new JComboBox(addressOfInterest);
		GridBagConstraints c6 = new GridBagConstraints();
		c6.insets = new Insets(5, 5, 0, 5);
		c6.fill = GridBagConstraints.HORIZONTAL;
		c6.weightx = 0.25;
		c6.gridx = 0;
		c6.gridy = 7;
		frame.getContentPane().add(pointOfInterest, c6);

		labelProximity = new JLabel("Proximity (Miles)");
		labelProximity.setFont(new Font("Tahoma", Font.BOLD, 11));
		GridBagConstraints gbc_labelProximity = new GridBagConstraints();
		gbc_labelProximity.insets = new Insets(0, 0, 5, 5);
		gbc_labelProximity.weightx = 0.25;
		gbc_labelProximity.gridx = 1;
		gbc_labelProximity.gridy = 6;
		frame.getContentPane().add(labelProximity, gbc_labelProximity);

		String[] radius = { "5", "10", "20", "30", "50" };
		proximity = new JComboBox(radius);
		GridBagConstraints c7 = new GridBagConstraints();
		c7.insets = new Insets(5, 5, 0, 5);
		c7.fill = GridBagConstraints.HORIZONTAL;
		c7.weightx = 0.25;
		c7.gridx = 1;
		c7.gridy = 7;
		frame.getContentPane().add(proximity, c7);

		String[] conditions = { "AND", "OR" };
		btnSearch = new JButton("Search");
		btnSearch.setFont(new Font("Tahoma", Font.BOLD, 12));
		GridBagConstraints gbc_btnSearch = new GridBagConstraints();
		gbc_btnSearch.gridheight = 3;
		gbc_btnSearch.insets = new Insets(5, 5, 0, 0);
		gbc_btnSearch.weightx = 0.25;
		gbc_btnSearch.gridx = 3;
		gbc_btnSearch.gridy = 4;
		frame.getContentPane().add(btnSearch, gbc_btnSearch);

		if (MongoDbHelper.open()) {
			setupMainCategories();
			setupAttributes();
			setupBusinessTable();
			setupJButton();
		}
	}

	private void setupJButton() {
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int address = pointOfInterest.getSelectedIndex() - 1;
				if (address >= 0) {
					String longLatt = pointsOfinterests[address][1];
					String longitude = longLatt.split(",")[0].trim();
					String lattitude = longLatt.split(",")[1].trim();
					int proximityMiles = Integer.parseInt((String) proximity.getSelectedItem());

					populateBusiness(longitude, lattitude, proximityMiles);
				}
			}
		});
	}

	private void setupMainCategories() {
		mainCategoryList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					populateAttributes();
				}
			}
		});
	}

	private void setupAttributes() {
		attributesList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					int address = pointOfInterest.getSelectedIndex() - 1;
					if (address >= 0) {
						String longLatt = pointsOfinterests[address][1];
						String longitude = longLatt.split(",")[0].trim();
						String lattitude = longLatt.split(",")[1].trim();
						int proximityMiles = Integer.parseInt((String) proximity.getSelectedItem());

						populateBusiness(longitude, lattitude, proximityMiles);
					} else {
						populateBusiness(null, null, 0);
					}
				}
			}
		});
	}

	private void populateAttributes() {
		Set<String> selectedMainCategories = Helper.removeDuplicates(mainCategoryList.getSelectedValuesList());

		tModel.setRowCount(0);
		attributesList.removeAll();
		if (selectedMainCategories.size() > 0) {
			// Not selected means OR
			String cond = "or";
			ArrayList<String> flatAttributes = MongoDbHelper.getAttributes(selectedMainCategories, cond);
			attributesList.setListData((flatAttributes.toArray(new String[flatAttributes.size()])));
		}
	}

	private void populateBusiness(String longitude, String lattitude, int proximity) {

		Set<String> selectedMainCategories = Helper.removeDuplicates(mainCategoryList.getSelectedValuesList());
		List<String> selectedAttributes = attributesList.getSelectedValuesList();
		tModel.setRowCount(0);

		if (selectedAttributes.size() > 0) {
			String cond1 = "or";
			String cond2 = "or";
			if (toggleAttributeCondition.isSelected()) {
				cond2 = "and";
			}

			if (longitude != null && lattitude != null && proximity != 0) {
				for (Document document : MongoDbHelper.getLocalBusinesses(selectedMainCategories, selectedAttributes, cond1, cond2, longitude, lattitude,
						proximity)) {
					tModel.addRow(new Object[] { document.get("business_id"), document.get("name"), document.get("city"), document.get("state"),
							document.get("stars") });
				}
			} else {
				for (Document document : MongoDbHelper.getBusinesses(selectedMainCategories, selectedAttributes, cond1, cond2)) {
					tModel.addRow(new Object[] { document.get("business_id"), document.get("name"), document.get("city"), document.get("state"),
							document.get("stars") });
				}
			}
		} else if (longitude != null && lattitude != null && proximity != 0) {
			for (Document document : MongoDbHelper.getLocalBusinesses(null, null, null, null, longitude, lattitude, proximity)) {
				tModel.addRow(
						new Object[] { document.get("business_id"), document.get("name"), document.get("city"), document.get("state"), document.get("stars") });
			}
		} else {
			// Nothing selected.
			System.out.println("Nothing selected.");
		}
	}

	private void setupBusinessTable() {
		filteredBusinesses.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent evnt) {
				if (evnt.getClickCount() == 1) {
					System.out.println("Row: " + filteredBusinesses.getSelectedRow());
					String businessId = (String) tModel.getValueAt(filteredBusinesses.getSelectedRow(), 0);

					DefaultTableModel model = new DefaultTableModel() {

						@Override
						public boolean isCellEditable(int row, int column) {
							return false;
						}
					};

					JTable reviewsTable = new JTable(model);
					JFrame frame2 = new JFrame();
					frame2.getContentPane().setLayout(new BorderLayout());
					frame2.getContentPane().add(new JScrollPane(reviewsTable));
					frame2.pack();
					frame2.setLocationRelativeTo(null);
					frame2.setVisible(true);
					frame2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

					model.addColumn("Review Date");
					model.addColumn("Stars");
					model.addColumn("Username");
					model.addColumn("Comment");
					model.addColumn("Useful Votes");

					try {
						model.setRowCount(0);
						AggregateIterable<Document> reviews = MongoDbHelper.getReviews(businessId);
						reviews.forEach(new Block<Document>() {

							@Override
							public void apply(final Document document) {
								model.addRow(new Object[] { document.getString("date"), document.get("stars"),
										(((ArrayList<Document>) document.get("user_data")).get(0)).get("name"), document.getString("text"),
										((Document) document.get("votes")).get("useful") });
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

}
